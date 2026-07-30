/*
 *  This file is part of lib1090.
 *  Copyright (C) 2026 SeRo Systems GmbH
 *
 *  lib1090 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  lib1090 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with de.serosystems.lib1090.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.serosystems.example;

import de.serosystems.lib1090.Position;
import de.serosystems.lib1090.StatefulModeSDecoder;
import de.serosystems.lib1090.Tools;
import de.serosystems.lib1090.exceptions.BadFormatException;
import de.serosystems.lib1090.exceptions.UnspecifiedFormatError;
import de.serosystems.lib1090.msgs.ModeSDownlinkMsg;
import de.serosystems.lib1090.msgs.adsb.*;
import de.serosystems.lib1090.msgs.modes.*;

import java.util.Scanner;

/**
 * ADS-B decoder example: It reads STDIN line-by-line. It should be fed with
 * comma-separated timestamp, receiver latitude, receiver longitude and the
 * raw Mode S/ADS-B message. Receiver coordinates can be omitted. In that
 * case, surface position messages cannot be decoded properly and plausibility
 * checks for positions are limited.
 *
 * Example input:
 *
 * 1,8d4b19f39911088090641010b9b0
 * 2,8d4ca513587153a8184a2fb5adeb
 * 3,8d3413c399014e23c80f947ce87c
 * 4,5d4ca88c079afe
 * 5,a0001838ca3e51f0a8000047a36a
 * 6,8d47a36a58c38668ffb55f000000
 * 7,5d506c28000000
 * 8,a8000102fe81c1000000004401e3
 * 9,a0001839000000000000004401e3
 *
 * @author Matthias Schäfer (schaefer@sero-systems.de)
 * @author Markus Fuchs (fuchs@opensky-network.org)
 */
public class ExampleDecoder {
	// The ModeSDecoder does all the magic for us
	private final StatefulModeSDecoder decoder = new StatefulModeSDecoder();

	/**
	 *
	 * @param timestamp in milliseconds since epoch
	 * @param raw Mode S messages as hex string
	 * @param receiver the location of the receiver for sanity checks on decoded positions (optional)
	 */
	public void decodeMsg(long timestamp, String raw, Position receiver) {
		ModeSDownlinkMsg msg;
		try {
			msg = decoder.decode(raw, timestamp);
		} catch (BadFormatException e) {
			System.out.println("Malformed message! Skipping it. Message: "+e.getMessage());
			return;
		} catch (UnspecifiedFormatError e) {
			System.out.println("Unspecified message! Skipping it...");
			return;
		}

		String icao24 = msg.getAddress().getHexAddress();

		// check for erroneous messages; some receivers set
		// parity field to the result of the CRC polynomial division
		if (msg.getParity() == 0 || msg.checkParity()) { // CRC is ok

			// now check the message type
			switch (msg.getType()) {
			case ADSB_AIRBORN_POSITION_V0:
			case ADSB_AIRBORN_POSITION_V1:
			case ADSB_AIRBORN_POSITION_V2:
			case ADSB_AIRBORN_POSITION_V3:
				AirbornePositionMsg ap = (AirbornePositionMsg) msg;
				System.out.print("["+icao24+"]: ");

				// use CPR to decode position
				// CPR needs at least 2 positions or a reference, otherwise we get null here
				Position c0 = decoder.extractPosition(msg.getAddress(), ap, receiver);
				if (c0 == null)
					System.out.println("Cannot decode position yet.");
				else
					System.out.println("Now at position (" + c0.getLatitude() + "," + c0.getLongitude() + ")");
				System.out.println("          Horizontal containment radius limit/protection level: " +
						ap.getHorizontalContainmentRadiusLimit() + " m");

				if (ap.hasValidAltitude()) {
					System.out.println("          Altitude: " + ap.getAltitude() + " ft");
					System.out.println("          Altitude Reference System: " + ap.getAltitudeType());
				}

				Double geoMinusBaro = decoder.getDiffBaroAlt(msg);
				if (ap.hasValidAltitude() && ap.getAltitudeType() == Position.AltitudeType.BAROMETRIC_ALTITUDE && geoMinusBaro != null) {
					System.out.println("          Height (geom.): " + ap.getAltitude() + geoMinusBaro + " ft");
				}

				System.out.println("          Navigation Integrity Category: " + ap.getNIC());
				System.out.println("          Surveillance status: " + ap.getSurveillanceStatusDescription());

				// we want to inspect fields for ADS-B of different versions
				switch(msg.getType()) {
					case ADSB_AIRBORN_POSITION_V0:
						// NACp and SIL for newer ADS-B versions contained in operational status message
						System.out.println("          Navigation Accuracy Category for position (NACp): " + ap.getNACp());
						System.out.println("          Position Uncertainty (based on NACp): " + ap.getPositionUncertainty());
						System.out.println("          Surveillance Integrity Level (SIL): " + ap.getSIL());
						break;
					case ADSB_AIRBORN_POSITION_V2:
						AirbornePositionV2Msg ap2 = (AirbornePositionV2Msg) msg;
						System.out.println("          NIC supplement B set: " + ap2.hasNICSupplementB());
						break;
					case ADSB_AIRBORN_POSITION_V3:
						AirbornePositionV3Msg ap3 = (AirbornePositionV3Msg) msg;
						System.out.println("          NIC supplement B set: " + ap3.hasNICSupplementB());
						break;
				}
				break;
			case ADSB_SURFACE_POSITION_V0:
			case ADSB_SURFACE_POSITION_V1:
			case ADSB_SURFACE_POSITION_V2:
			case ADSB_SURFACE_POSITION_V3:
				SurfacePositionMsg surfacePosition = (SurfacePositionMsg) msg;
				System.out.print("["+icao24+"]: ");

				Position sPos0 = decoder.extractPosition(msg.getAddress(), surfacePosition, receiver);
				// decode the position if possible; prior position needed
				if (sPos0 == null)
					System.out.println("Cannot decode position yet or no reference available (yet).");
				else
					System.out.println("Now at position (" + sPos0.getLatitude() + "," + sPos0.getLongitude() + ")");

				if (surfacePosition.hasValidHeading())
					System.out.println("          Heading: " + surfacePosition.getHeading() + "°");
				System.out.println("          Airplane is on the ground.");

				if (surfacePosition.hasGroundSpeed()) {
					System.out.println("          Ground speed: " + surfacePosition.getGroundSpeed() + "kt");
					System.out.println("          Ground speed resolution: " + surfacePosition.getGroundSpeedResolution() + "kt");
				}

				System.out.println("          Horizontal containment radius limit/protection level is " +
						surfacePosition.getHorizontalContainmentRadiusLimit() + "m");
				System.out.println("          Navigation Integrity Category: " + surfacePosition.getNIC());

				// we want to inspect fields for ADS-B of different versions
				switch(msg.getType()) {
					case ADSB_SURFACE_POSITION_V0:
						SurfacePositionV0Msg sp0 = (SurfacePositionV0Msg) msg;
						// NACp and SIL for newer ADS-B versions contained in operational status message
						// Use the following only with version 0 as the others are more accurate
						System.out.println("          Navigation Accuracy Category for position (NACp): " + sp0.getNACp());
						System.out.println("          Position Uncertainty (based on NACp): " + sp0.getPositionUncertainty() + "m");
						System.out.println("          Surveillance Integrity Level (SIL): " + sp0.getSIL());
						break;
				}

				break;
			case ADSB_EMERGENCY_V0V1:
			case ADSB_EMERGENCY_V2:
				EmergencyOrPriorityStatusMsg status = (EmergencyOrPriorityStatusMsg) msg;
				System.out.println("["+icao24+"]: "+status.getEmergencyStateText());
				if (status instanceof EmergencyOrPriorityStatusV2Msg) {
					System.out.println("          Mode A code is "+((EmergencyOrPriorityStatusV2Msg) status).getIdentity());
				}
				break;
			case ADSB_MODE_A_CODE_V1:
				ModeACodeV1Msg modeACode = (ModeACodeV1Msg) msg;
				System.out.println("["+icao24+"]: Mode A code is "+modeACode.getIdentity());
				break;
			case ADSB_AIRSPEED_V0:
			case ADSB_AIRSPEED_V1:
			case ADSB_AIRSPEED_V2:
				AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg) msg;
				System.out.println("["+icao24+"]: Airspeed: "+
						(airspeed.hasAirspeed() ? airspeed.getAirspeed()+" kt" : "unknown"));

				if (decoder.getAdsbVersion(msg) == 0) {
					// version 0 flag indicates true or magnetic north
					System.out.println("          Heading: " + airspeed.getHeading() + "° relative to " +
							(airspeed.hasHeadingStatusFlag() ? "magnetic north" : "true north"));
				} else {
					// version 1+ flag indicates if heading is available at all
					System.out.println("          Heading: "+
							(airspeed.hasHeadingStatusFlag() ? airspeed.getHeading()+"°" : "unknown"));
				}

				if (airspeed.hasVerticalRate())
					System.out.println("          Vertical rate: "+
							(airspeed.hasVerticalRate() ? airspeed.getVerticalRate()+" ft/min" : "unknown"));
				break;
			case ADSB_IDENTIFICATION_V0:
			case ADSB_IDENTIFICATION_V1:
			case ADSB_IDENTIFICATION_V2:
			case ADSB_IDENTIFICATION_V3:
				IdentificationMsg ident = (IdentificationMsg) msg;
				System.out.println("["+icao24+"]: Callsign: "+new String(ident.getIdentification()));
				System.out.println("          Category: "+ident.getEmitterCategory());
				break;
			case ADSB_STATUS_V0:
				OperationalStatusV0Msg opstat0 = (OperationalStatusV0Msg) msg;
				System.out.println("["+icao24+"]: Using ADS-B version "+opstat0.getVersion());
				System.out.println("          Has operational TCAS: "+ opstat0.hasOperationalTCAS());
				System.out.println("          Has operational CDTI: "+ opstat0.hasOperationalCDTI());
				break;
			case ADSB_AIRBORN_STATUS_V1:
			case ADSB_AIRBORN_STATUS_V2:
			case ADSB_AIRBORN_STATUS_V3:
				AirborneOperationalStatusMsg opstatA = (AirborneOperationalStatusMsg) msg;
				System.out.println("["+icao24+"]: Using ADS-B version "+opstatA.getVersion());
				System.out.println("          Navigation Accuracy Category for position (NACp): " + opstatA.getNACpEncoded());
				System.out.println("          Position Uncertainty (based on NACp): " + opstatA.getPositionUncertainty());
				System.out.println("          Has NIC supplement A: " + opstatA.hasNICSupplementA());
				System.out.println("          Surveillance/Source Integrity Level (SIL): " + opstatA.getSILEncoded());
				System.out.println("          Has 1090 ES In: " + opstatA.has1090ESIn());
				System.out.println("          IDENT switch active: " + opstatA.hasActiveIDENTSwitch());
				System.out.println("          Has operational TCAS: " + opstatA.hasOperationalTCAS());
				System.out.println("          Has TCAS resolution advisory: " + opstatA.hasTCASResolutionAdvisory());

				if (msg instanceof AirborneOperationalStatusV1V2Msg) {
					AirborneOperationalStatusV1V2Msg opstatV1V2 = (AirborneOperationalStatusV1V2Msg) msg;
					System.out.println("          Barometric altitude cross-checked: " + opstatV1V2.getBarometricAltitudeIntegrityCode());
					System.out.println("          Horizontal reference: " + (opstatV1V2.getHorizontalReferenceDirection() ? "magnetic north" : "true north"));
					System.out.println("          Supports air-referenced velocity reports: " + opstatV1V2.hasAirReferencedVelocity());
				}

				if (msg instanceof OperationalStatusV2V3Msg) {
					OperationalStatusV2V3Msg opstatV2V3 = (OperationalStatusV2V3Msg) msg;
					System.out.println("          System design assurance: " + opstatV2V3.getSDAEncoded());
					System.out.println("          Has UAT in: " + opstatV2V3.hasUATIn());
					System.out.println("          Has SIL supplement: " + opstatV2V3.hasSILSupplement());
					System.out.println("          Uses single antenna: " + opstatV2V3.hasSingleAntenna());
				}

				if (msg instanceof AirborneOperationalStatusV2V3Msg) {
					System.out.println("          Geometric vertical accuracy: " + ((AirborneOperationalStatusV2V3Msg) msg).getGeometricVerticalAccuracy() + "m");
				}

				if (msg instanceof AirborneOperationalStatusV3Msg) {
					AirborneOperationalStatusV3Msg opstatV3 = (AirborneOperationalStatusV3Msg) msg;
					System.out.println("          Transponder side indication: " + opstatV3.getTransponderSideIndicationEncoded());
					System.out.println("          Tx power: " + opstatV3.getTxPowerEncoded());
					System.out.println("          Reduced Capability Equipment: " + opstatV3.getReducedCapabilityEquipmentEncoded());
					System.out.println("          Detect and Avoid: " + opstatV3.getDetectAndAvoidEncoded());
				}

				break;
			case ADSB_SURFACE_STATUS_V1:
			case ADSB_SURFACE_STATUS_V2:
			case ADSB_SURFACE_STATUS_V3:
				SurfaceOperationalStatusMsg opstatS = (SurfaceOperationalStatusMsg) msg;

				System.out.println("["+icao24+"]: Using ADS-B version "+opstatS.getVersion());

				System.out.println("          Horizontal reference: " + (opstatS.getHorizontalReferenceDirection() ? "magnetic north" : "true north"));
				System.out.println("          Navigation Accuracy Category for position (NACp): " + opstatS.getNACpEncoded());
				System.out.println("          Position Uncertainty (based on NACp): " + opstatS.getPositionUncertainty());
				System.out.println("          Has NIC supplement A: " + opstatS.hasNICSupplementA());
				System.out.println("          Surveillance/Source Integrity Level (SIL): " + opstatS.getSILEncoded());
				System.out.println("          Has 1090 ES In: " + opstatS.has1090ESIn());
				System.out.println("          IDENT switch active: " + opstatS.hasActiveIDENTSwitch());
				System.out.println("          Has TCAS resolution advisory: " + opstatS.hasTCASResolutionAdvisory());
				System.out.println("          Airplane length: " + opstatS.getAirplaneLength() + "m");
				System.out.println("          Airplane width: " + opstatS.getAirplaneWidth() + "m");
				System.out.println("          Low (<70W) TX power: " + opstatS.hasLowTxPower());
				System.out.println("          Has track heading info: " + opstatS.hasTrackHeading());

				if (msg instanceof SurfaceOperationalStatusV2V3Msg) {
					SurfaceOperationalStatusV2V3Msg opstatSV2V3 = (SurfaceOperationalStatusV2V3Msg) msg;
					System.out.println("          Has NIC supplement C: " + opstatSV2V3.getNICSupplementC());
					System.out.println("          Navigation Accuracy Category for velocity (NACv): " + opstatSV2V3.getNACv());
					System.out.println("          Encoded GPS antenna offset: " + opstatSV2V3.getGPSAntennaOffsetEncoded());
				}

				if (msg instanceof OperationalStatusV2V3Msg) {
					OperationalStatusV2V3Msg opstatV2V3 = (OperationalStatusV2V3Msg) msg;
					System.out.println("          System design assurance: " + opstatV2V3.getSDAEncoded());
					System.out.println("          Has UAT in: " + opstatV2V3.hasUATIn());
					System.out.println("          Has SIL supplement: " + opstatV2V3.hasSILSupplement());
					System.out.println("          Uses single antenna: " + opstatV2V3.hasSingleAntenna());
				}

				break;
			case ADSB_TCAS:
				TCASResolutionAdvisoryMsg tcas = (TCASResolutionAdvisoryMsg) msg;
				System.out.println("["+icao24+"]: TCAS Resolution Advisory completed: "+tcas.hasRATerminated());
				System.out.println("          Threat type is "+tcas.getThreatType());
				if (tcas.getThreatType() == 1) // it's a icao24 address
					System.out.println("          Threat identity is 0x"+String.format("%06x", tcas.getThreatIdentity()));
				break;
			case ADSB_VELOCITY_V0:
			case ADSB_VELOCITY_V1:
			case ADSB_VELOCITY_V2:
			case ADSB_VELOCITY_V3:
				VelocityOverGroundMsg veloc = (VelocityOverGroundMsg) msg;
				System.out.println("["+icao24+"]: Ground Speed: "+(veloc.hasVelocity() ? veloc.getGroundSpeed() : "unknown")+" kt");
				System.out.println("          True Track: "+(veloc.hasVelocity() ? veloc.getTrueTrackAngle() : "unknown")+" °");
				System.out.println("          Vertical rate: "+(veloc.hasVerticalRate() ? veloc.getVerticalRate() : "unknown")+" ft/min");

				// the IFR flag is only used in ADS-B version 1. Although equipage is low, we still support it
				if (decoder.getAdsbVersion(msg) == 1)
					System.out.println("          Has IFR capability: " + ((IFRCapabilityMsg) veloc).hasIFRCapability());

				break;
			case ADSB_TARGET_STATE_AND_STATUS_V1:
			case ADSB_TARGET_STATE_AND_STATUS_V2:
				System.out.println("["+icao24+"]: Target State and Status reported");
				if (msg instanceof TargetStateAndStatusV1Msg) {
					TargetStateAndStatusV1Msg tStatus = (TargetStateAndStatusV1Msg) msg;
					System.out.println("          Navigation Accuracy Category for position (NACp): " + tStatus.getNACpEncoded());
					System.out.println("          Has operational TCAS: " + tStatus.hasOperationalTCAS());
					System.out.println("          Surveillance/Source Integrity Level (SIL): " + tStatus.getSILEncoded());
					System.out.println("          Barometric altitude cross-checked: " + tStatus.getBarometricAltitudeIntegrityCode());
					if (tStatus.hasSelectedAltitude()) {
						System.out.println("          Selected altitude: " + tStatus.getSelectedAltitude() + " ft");
					} else {
						System.out.println("          No selected altitude info");
					}
					if (tStatus.hasSelectedHeading()) {
						System.out.println("          Selected heading: " + tStatus.getSelectedHeading() + "°");
					} else {
						System.out.println("          No selected heading info");
					}
				} else {

					TargetStateAndStatusV2Msg tStatus = (TargetStateAndStatusV2Msg) msg;
					System.out.println("          Navigation Accuracy Category for position (NACp): " + tStatus.getNACpEncoded());
					System.out.println("          Has operational TCAS: " + tStatus.hasOperationalTCAS());
					System.out.println("          Surveillance/Source Integrity Level (SIL): " + tStatus.getSILEncoded());
					System.out.println("          Has SIL supplement: " + tStatus.hasSILSupplement());
					System.out.println("          Barometric altitude cross-checked: " + tStatus.getBarometricAltitudeIntegrityCode());

					System.out.printf("          Selected altitude is derived from %s\n", tStatus.isFMSSelectedAltitude() ? "FMS" : "MCP/FCU");
					if (tStatus.hasSelectedAltitude()) {
						System.out.println("          Selected altitude: " + tStatus.getSelectedAltitude() + " ft");
					} else {
						System.out.println("          No selected altitude info");
					}

					if (tStatus.hasBarometricPressureSetting()) {
						System.out.println("          Barometric pressure setting (minus 800 mbar): " + tStatus.getBarometricPressureSetting() + " mbar");
					} else {
						System.out.println("          No barometric pressure setting info");
					}

					if (tStatus.hasSelectedHeading()) {
						System.out.println("          Selected heading: " + tStatus.getSelectedHeading() + "°");
					} else {
						System.out.println("          No selected heading info");
					}
					if (tStatus.hasMode()) {
						System.out.printf("          Autopilot is%s enganged\n", tStatus.hasAutopilotEngaged() ? "" : " not");
						System.out.printf("          VNAV mode is%s enganged\n", tStatus.hasVNAVModeEngaged() ? "" : " not");
						System.out.printf("          Altitude hold mode is%s enganged\n", tStatus.hasActiveAltitudeHoldMode() ? "" : " not");
						System.out.printf("          Approach mode is%s enganged\n", tStatus.hasActiveApproachMode() ? "" : " not");
						System.out.printf("          LNAV mode is%s enganged\n", tStatus.hasLNAVModeEngaged() ? "" : " not");
					} else {
						System.out.println("          No MCP/FCU mode info");
					}
				}

				break;
			case ADSB_HVA_POSITION:
				HVAPositionMsg hvaPos = (HVAPositionMsg) msg;
				System.out.println("["+icao24+"]: HVA Position reported");
				System.out.println("          Geometric altitude (HAE): " + (hvaPos.hasHVAGeometricAltitude() ? hvaPos.getHVAGeometricAltitude() : "unknown") + " ft");
				System.out.println("          Latitude: " + hvaPos.getHVALatitude() + "°");
				System.out.println("          Longitude: " + hvaPos.getHVALongitude() + "°");
				break;
			case ADSB_HVA_VELOCITY:
				HVAVelocityMsg hvaVel = (HVAVelocityMsg) msg;
				System.out.println("["+icao24+"]: HVA Velocity reported");
				System.out.println("          East/West velocity: " + (hvaVel.hasHVAEastWestVelocity() ? hvaVel.getHVAEastWestVelocity() : "unknown") + " kt");
				System.out.println("          North/South velocity: " + (hvaVel.hasHVANorthSouthVelocity() ? hvaVel.getHVANorthSouthVelocity() : "unknown") + " kt");
				System.out.println("          Vertical rate: " + (hvaVel.hasHVAVerticalRate() ? hvaVel.getHVAVerticalRate() : "unknown") + " ft/min");
				if (hvaVel.hasPIC())
					System.out.println("          Radius of Containment: < " + hvaVel.getRadiusOfContainment() + " m");
				break;
			case ADSB_WX_AIREP_AIRCRAFT_STATE:
				WxAIREPAircraftStateMsg wxState = (WxAIREPAircraftStateMsg) msg;
				System.out.println("["+icao24+"]: Wx AIREP Aircraft State reported");
				System.out.println("          Aircraft configuration: " + wxState.getAircraftConfigurationEncoded());
				System.out.println("          Aircraft type: " + (wxState.hasAircraftType() ? String.valueOf(wxState.getAircraftType()) : "unknown"));
				System.out.println("          Gross weight: " + (wxState.hasGrossWeight() ? ">= " + wxState.getGrossWeight() : "unknown") + " lbs");
				System.out.println("          Wingspan: " + (wxState.hasWingspan() ? ">= " + wxState.getWingspan() : "unknown") + " ft");
				break;
			case ADSB_WX_AIREP_WEATHER_STATE:
				WxAIREPWeatherStateMsg wxWeather = (WxAIREPWeatherStateMsg) msg;
				System.out.println("["+icao24+"]: Wx AIREP Weather State reported");
				System.out.println("          Icing status: " + wxWeather.getIcingStatusEncoded());
				System.out.println("          Wind quality indicator: " + wxWeather.getWindQualityIndicatorEncoded());
				System.out.println("          Wind speed: " + (wxWeather.hasWindSpeed() ? ">= " + wxWeather.getWindSpeed() : "unknown") + " kt");
				System.out.println("          Wind direction: " + (wxWeather.hasWindDirection() ? ">= " + wxWeather.getWindDirection() : "unknown") + "°");
				System.out.println("          Air temperature (" + (wxWeather.getAirTemperatureType() ? "static" : "total") + "): " +
						(wxWeather.hasAirTemperature() ? ">= " + wxWeather.getAirTemperature() : "unknown") + "°C");
				System.out.println("          Airspeed (" + (wxWeather.getAirspeedType() ? "TAS" : "IAS") + "): " +
						(wxWeather.hasAirspeed() ? ">= " + wxWeather.getAirspeed() : "unknown") + " kt");
				break;
			case ADSB_WX_AIREP_ALTERNATE_WEATHER_STATE:
				WxAIREPAlternateWeatherStateMsg wxAltWeather = (WxAIREPAlternateWeatherStateMsg) msg;
				System.out.println("["+icao24+"]: Wx AIREP Alternate Weather State reported");
				System.out.println("          Icing status: " + wxAltWeather.getIcingStatusEncoded());
				System.out.println("          Roll angle: " + (wxAltWeather.hasRollAngle() ? wxAltWeather.getRollAngle() : "unknown") + "°");
				System.out.println("          Heading (" + (wxAltWeather.getHeadingType() ? "magnetic" : "true") + "): " +
						(wxAltWeather.hasHeading() ? ">= " + wxAltWeather.getHeading() : "unknown") + "°");
				System.out.println("          Air temperature (" + (wxAltWeather.getAirTemperatureType() ? "static" : "total") + "): " +
						(wxAltWeather.hasAirTemperature() ? ">= " + wxAltWeather.getAirTemperature() : "unknown") + "°C");
				System.out.println("          Airspeed (" + (wxAltWeather.getAirspeedType() ? "TAS" : "IAS") + "): " +
						(wxAltWeather.hasAirspeed() ? ">= " + wxAltWeather.getAirspeed() : "unknown") + " kt");
				break;
			case EXTENDED_SQUITTER:
				System.out.println("["+icao24+"]: Unknown extended squitter with type code "+((ExtendedSquitter)msg).getFormatTypeCode()+"!");
				break;
			}
		}
		else if (msg.getDownlinkFormat() != 17) { // CRC failed
			switch (msg.getType()) {
			case MODES_REPLY:
				System.out.println("["+icao24+"]: Unknown message with DF "+msg.getDownlinkFormat());
				break;
			case SHORT_ACAS:
				ShortACAS acas = (ShortACAS)msg;
				System.out.println("["+icao24+"]: Altitude is "+acas.getAltitude()+"ft and ACAS is "+
						(acas.hasOperatingACAS() ? "operating." : "not operating."));
				System.out.println("          A/C is "+(acas.isAirborne() ? "airborne" : "on the ground")+
						" and sensitivity level is "+acas.getSensitivityLevel());
				break;
			case ALTITUDE_REPLY:
				AltitudeReply alti = (AltitudeReply)msg;
				System.out.println("["+icao24+"]: Short altitude reply: "+alti.getAltitude()+"ft");
				break;
			case IDENTIFY_REPLY:
				IdentifyReply identify = (IdentifyReply)msg;
				System.out.println("["+icao24+"]: Short identify reply: "+identify.getIdentity());
				break;
			case ALL_CALL_REPLY:
				AllCallReply allcall = (AllCallReply)msg;
				System.out.println("["+icao24+"]: All-call reply for "+ Tools.toHexString(allcall.getInterrogatorCode())+
						" ("+(allcall.hasValidInterrogatorCode()?"valid":"invalid")+")");
				break;
			case LONG_ACAS:
				LongACAS long_acas = (LongACAS)msg;
				System.out.println("["+icao24+"]: Altitude is "+long_acas.getAltitude()+"ft and ACAS is "+
						(long_acas.hasOperatingACAS() ? "operating." : "not operating."));
				System.out.println("          A/C is "+(long_acas.isAirborne() ? "airborne" : "on the ground")+
						" and sensitivity level is "+long_acas.getSensitivityLevel());
				System.out.println("          RAC is "+(long_acas.hasValidRAC() ? "valid" : "not valid")+
						" and is "+long_acas.getResolutionAdvisoryComplement()+" (MTE="+long_acas.hasMultipleThreats()+")");
				System.out.println("          Maximum airspeed is "+long_acas.getMaximumAirspeed()+"kn.");
				break;
			case MILITARY_EXTENDED_SQUITTER:
				MilitaryExtendedSquitter mil = (MilitaryExtendedSquitter)msg;
				System.out.println("["+icao24+"]: Military ES of application "+mil.getFirstField());
				System.out.println("          Message is 0x"+ Tools.toHexString(mil.getMessage()));
				break;
			case COMM_B_ALTITUDE_REPLY:
				CommBAltitudeReply commBaltitude = (CommBAltitudeReply)msg;
				System.out.println("["+icao24+"]: Long altitude reply: "+commBaltitude.getAltitude()+"ft");
				break;
			case COMM_B_IDENTIFY_REPLY:
				CommBIdentifyReply commBidentify = (CommBIdentifyReply)msg;
				System.out.println("["+icao24+"]: Long identify reply: "+commBidentify.getIdentity());
				break;
			case COMM_D_ELM:
				CommDExtendedLengthMsg commDELM = (CommDExtendedLengthMsg)msg;
				System.out.println("["+icao24+"]: ELM message w/ sequence no "+commDELM.getSequenceNumber()+
						" (ACK: "+commDELM.isAck()+")");
				System.out.println("          Message is 0x"+ Tools.toHexString(commDELM.getMessage()));
				break;
			default:
			}
		}
		else {
			System.out.println("Message contains biterrors.");
		}
	}

	public static void main(String[] args) throws Exception {
		// iterate over STDIN
		Scanner sc = new Scanner(System.in, "UTF-8");
		ExampleDecoder dec = new ExampleDecoder();
		Position rec = new Position(0., 0., 0.);
		while(sc.hasNext()) {
		  String[] values = sc.nextLine().split(",");

		  if (values.length == 5) {
			  // time,serial,lat,lon,msg
			  rec.setLatitude(Double.parseDouble(values[2]));
			  rec.setLongitude(Double.parseDouble(values[3]));
			  dec.decodeMsg((long) Double.parseDouble(values[0])*1000, values[4], rec);
		  } else if (values.length == 4) {
			  // time,lat,lon,msg
			  rec.setLatitude(Double.parseDouble(values[1]));
			  rec.setLongitude(Double.parseDouble(values[2]));
			  dec.decodeMsg((long) Double.parseDouble(values[0])*1000, values[3], rec);
		  } else if (values.length == 2) {
			  // time,msg
			  dec.decodeMsg((long) Double.parseDouble(values[0])*1000, values[1], null);
		  }
		}
		sc.close();
	}
}
