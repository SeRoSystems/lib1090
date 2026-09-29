# Changelog for lib1090

## v5.0.0

This release is a major refactoring of the message class hierarchy and adds support for ADS-B v3, along with
matching updates to ADS-R and TIS-B decoding. It also folds in a series of correctness fixes to Operational Status
and CPR decoding made since v4.1.3.

### Breaking Changes

- Message classes moved from deep per-version inheritance chains to flat sibling classes implementing shared
  interfaces (new `msgs.squitter` package); code relying on e.g.
  `AirbornePositionV2Msg instanceof AirbornePositionV1Msg`
  will need to be adapted
- Removed `ModeSDownlinkMsg.subtype` enum and all per-class `getType()` overrides; message identity is now
  determined via `instanceof`/`getClass()`
- Message classes now store raw encoded values and interpret them only through accessors, instead of storing
  interpreted fields directly
- `StatefulModeSDecoder.decode(...)` now takes a mandatory, non-null `java.time.Instant` timestamp instead of `long`
- `StatefulModeSDecoder.decode(ModeSDownlinkMsg, Instant)` no longer declares `UnspecifiedFormatError`, which it
  cannot throw: the message has been parsed by then. A `catch` of that exception around this overload alone no longer
  compiles; the overloads taking a raw message still declare it
- The `ThreatIdentityData` constructor for altitude, range and bearing no longer declares `BadFormatException`, since
  an unassigned bearing no longer throws (see Bug Fixes). It throws `IllegalArgumentException` for a range outside
  [0, 127] or a bearing outside [0, 63], values its 7-bit and 6-bit fields cannot hold
- The registers that identify themselves in MB bits 1-8 reject a message that holds another register's code with a
  `BadFormatException`: `DataLinkCapabilityReport.decode()` (BDS 1,0), the `AircraftIdentification` constructor
  (BDS 2,0), which now declares it, and the `ACASActiveResolutionAdvisoryReport` constructor (BDS 3,0), whose
  `BadFormatException` used to stand for an unassigned threat bearing only (#165)
- `ThreatIdentityData.getRange()` returns the estimated range, (n - 1)/10 NM for code n from 2 to 126, where it
  returned the lower bound, 0.05 NM less; the return type is unchanged, so code relying on the lower bound now gets a
  value 0.05 NM higher without a compile error. Codes 1 (less than 0.05 NM) and 127 (greater than 12.55 NM) give
  `null`. `getBearing()` returns the middle of the bearing step, 6n - 3 degrees, as a `Float` instead of the
  `Float[]` with its lower and upper end. The new `getRangeInterval()` and `getBearingInterval()` give the steps as
  ICAO Annex 10 Volume IV §4.3.8.4.2.2.1.6.2 and §4.3.8.4.2.2.1.6.3 state them, as `getAltitudeInterval()` does for the
  binary altitude (#186)
- ACAS resolution advisory content, message bits 41-88, is decoded into the new package `msgs.acas`, one class per
  layout, since ACAS X uses a different one than TCAS (see Bug Fixes). `ACASActiveResolutionAdvisoryReport` (BDS 3,0),
  the 1090ES RA broadcast and `LongACAS` (DF=16) return it from `getResolutionAdvisory()`: a
  `TCASResolutionAdvisoryReport`, `ACASXResolutionAdvisoryReport` or `TCAS6ResolutionAdvisory` for an RA report or
  broadcast, a `TCASResolutionAdvisory` or `ACASXResolutionAdvisory` for a coordination reply, or `null` where the RA
  message format defines no layout. The interfaces `ResolutionAdvisory`, `ResolutionAdvisoryState` (with RAT and MTE)
  and `ResolutionAdvisoryReport` (with the threat identity) hold what the layouts share. The field accessors of the
  containers are gone: `getActiveRA()`, `getRACRecord()`, `hasRATerminated()`, `hasMultiThreatEncounter()`,
  `getThreatType()`, `getThreatIdentity()`, `getActiveResolutionAdvisories()`,
  `getResolutionAdvisoriesComplementsRecord()` and `getThreatIdentityData()` of the report and the broadcast, and
  `getActiveResolutionAdvisories()`, `getResolutionAdvisoryComplementEncoded()`, `noPassBelow()`, `noPassAbove()`,
  `noTurnLeft()`, `noTurnRight()`, `hasTerminated()` and `hasMultipleThreats()` of `LongACAS`; `isTCAS6()` is now
  `instanceof TCAS6ResolutionAdvisory`
- Renamed `TCASResolutionAdvisoryMsg` to `ACASResolutionAdvisoryMsg`, since it carries ACAS X content as well, and
  moved `ThreatIdentityData` from `msgs.bds` to `msgs.acas`
- `hasOperatingACAS()` of `ShortACAS` and `LongACAS` returns `Boolean`, `null` where the reply information does not
  tell: an acquisition reply (RI 8-15) and the codes not assigned or reserved (RI 5-7). Both classes now implement
  the new `AirAirSurveillanceReply`, which defines it with the other reply information accessors (see Bug Fixes)
- DF=19/AF=0 is no longer decoded as ADS-B by default; enable via `StatefulModeSDecoder.builder().decodeDf19Adsb(true)`
- Reassigned `serialVersionUID`s across message classes, breaking Java-serialization compatibility with objects
  serialized under earlier versions. In practice, Java serialization of a message never worked before, see Bug Fixes
- `StatefulModeSDecoder.decode(...)` now rejects extended squitters (DF=17, DF=18, and DF=19 if decoded as ADS-B)
  whose parity check fails, throwing `BadFormatException` before they reach the per-target state (ED-102B §2.2.4.5 a).
  Previously a corrupted message could change a target's ADS-B version, NIC supplements and CPR frames, and create
  state for a garbage address. Input that is already checked, or deliberately unchecked, can be decoded as before with
  `StatefulModeSDecoder.builder().checkParity(false)`
- Renamed `MLATSystemStatusMsg` to `SurfaceSystemStatusMsg`, the name ED-102B TABLE 2-74 gives TYPE Code 24
  subtype 1; "Multilateration System Status" was the version 2 name (ED-102A Table 2-77)
- Removed the deprecated `ModeSDownlinkMsg.CRC_polynomial`, a public array anyone could overwrite; use
  `ModeSDownlinkMsg.CRC_POLYNOMIAL`
- The `QualifiedAddress` constructors reject addresses that do not fit in 24 bits, 0 to 0xFFFFFF, with an
  `IllegalArgumentException`, every address in the AA field being 24 bits wide (ED-102B TABLE 2-8); decoded
  addresses always fit
- `QualifiedAddress` is no longer a nested class of `ModeSDownlinkMsg`; it now lives at the package level
- `QualifiedAddress` now also carries a `QualifiedAddress.Source` (transponder, non-transponder, TIS-B or ADS-R) that
  takes part in `equals`/`hashCode`, and its constructors take it as a third argument. The same address received via
  DF=17 and via ADS-R, for example, now yields two unequal addresses; code that keys its own per-target data on
  `QualifiedAddress` will see them as separate targets, as ED-102B requires for reports
- CPR local/global decoding now requires a timestamp (previously optional)
- Operational status messages no longer expose Capability Class Code and Operational Mode Code subfields directly.
  The actual information carried in these fields depend on a selector inside the field. In ADS-B v2 and before, only
  a single selector has been used, so the carried information was always the same and could be embedded into the message
  class. That changed with ADS-B v3 so the design was changed here. To get the raw value of the whole CC and OM field,
  there are accessors `getCapabilityClassCodeEncoded()` and `getOperationalModeCodeEncoded()`. Additionally, there
  are accessors `getCapabilityClass()` and `getOperationalMode()` that will return an object that gives access to the
  carried information. You will need `instanceof` and cast to the interface carrying the subfield you want (new
  package `msgs.squitter.opstatus` for the layouts and factories; the interfaces live in `msgs.squitter`)
- `getGPSAntennaOffsetEncoded()` now returns `int` rather than `byte`: the subfield is a full 8 bits, so a signed byte
  read an all-ones offset as `-1` instead of `255`
- Renamed operational status accessors so the prefix says what the value asserts: `is…` for a current state with the
  predicate word spelling out what `true` means, `supports…` for a reporting capability, `has…` only for a fixed
  property of the installation. `hasOperationalTCAS` → `isCollisionAvoidanceOperational`, `hasActiveIDENTSwitch` →
  `isIDENTSwitchActive`, `hasTCASResolutionAdvisory` → `isTCASResolutionAdvisoryActive`, `hasModeSReplyRateLimiting` →
  `isModeSReplyRateLimitingActive`, `hasRemainWellClearActive` → `isRemainWellClearActive`, `hasPositionOffsetApplied`
  → `isPositionOffsetApplied`, `hasReceivingATCServices` → `isReceivingATCServices`, `hasAirReferencedVelocity` →
  `supportsARVReport`, `hasTargetStateReport` → `supportsTSReport`, `hasLowTxPower` → `isB2Low` (the standard calls the
  subfield "B2 Low", and it is neither unconditional nor static), and
  `getTargetChangeReportCapabilityEncoded` → `getTCReportCapabilityLevelEncoded`
- Renamed three accessors library-wide, since the same members exist on position and target-state messages:
  `hasNICSupplement{A,B,C}()` → `getNICSupplement{A,B,C}()` and `hasSILSupplement()` → `getSILSupplement()` (the bit is
  the supplement value, not a predicate), and `getHorizontalReferenceDirection()` →
  `isHeadingReferencedToMagneticNorth()` (a boolean that was named like a direction; `true` means magnetic north)
- Dropped the `Info` suffix from the availability accessors of velocity, target state and status and surface
  operational status messages, in ADS-B, ADS-R and TIS-B alike: `hasAirspeedInfo` → `hasAirspeed`,
  `hasBarometricPressureSettingInfo` → `hasBarometricPressureSetting`, `hasModeInfo` → `hasMode`,
  `hasSelectedAltitudeInfo` → `hasSelectedAltitude`, `hasSelectedHeadingInfo` → `hasSelectedHeading`,
  `hasTrackHeadingInfo` → `hasTrackHeading`, `hasVelocityInfo` → `hasVelocity`, `hasVerticalRateInfo` →
  `hasVerticalRate`, and `hasGeoMinusBaroInfo`/`getGeoMinusBaro` → `hasDiffBaroAlt`/`getDiffBaroAlt` (see below).
  `SelectedVerticalIntention` (BDS 4,0) names its MCP/FCU mode status accessor `hasMode` as well
- `DataLinkCapabilityReport` (BDS 1,0) names its ACAS accessors as ICAO Annex 10 Volume IV (6th edition)
  §4.3.8.4.2.2.3 and Table 3-6 define the bits: `isTcasOperationalCoordinationMessage` → `isOCMTransmitCapability`,
  `getTcasExtendedVersionNumber` → `getACASTypeEncoded`, `isTcasInterfaceOperational` → `isACASOperating`,
  `isTcasHybridSurveillanceCapability` → `isACASHybridSurveillanceCapability`, `isTcasRataCapability` →
  `isACASGeneratingRAs` and `getTcasVersionNumber` → `getACASVersionNumber`, whose code 3 now stands for all later
  systems (#208)
- Removed `DataLinkCapabilityReport.isEnhancedSurveillanceCapability()`. It read MB bit 47, which ICAO Annex 10
  Volume IV (6th edition) Table 3-6 and DO-181E §2.2.19.1.12.6.2 do not define and ED-73F Table B-3-16a lists as
  reserved, so it answered false for every conformant transponder, and true for a Doc 9871 2nd edition one that
  supports DTE sub-address 6. Whether a transponder supports the EHS
  registers 4,0, 5,0 and 6,0 shows in the common usage GICB capability report (BDS 1,7),
  `CommonUsageGICBCapabilityReport.getCommonUsageGICBCapabilityReport()` (#159)
- `DataLinkCapabilityReport` is abstract, with the subclasses `DataLinkCapabilityReportV6` and
  `DataLinkCapabilityReportV0V5` for the two layouts of MB bits 41-56 (see Bug Fixes);
  `DataLinkCapabilityReport.decode()` picks the one the report's Mode S subnetwork version number selects and replaces
  the constructor; the constructors of the two subclasses throw `BadFormatException` for the other version.
  `isBasicDataFlashCapability()`, `isPhaseOverlayExtendedSquitterCapability()`, `isPhaseOverlayModeSCapability()`,
  `getActiveTransponderSideIndicator()` and `isChangeFlag()` are only on `DataLinkCapabilityReportV6`
- `getBarometricPressureSetting()` of target state and status messages returns the barometric pressure setting itself,
  800 to 1208 mb, as `SelectedVerticalIntention` (BDS 4,0) already did: the field carries the setting minus 800 mb
  (ED-102B §2.2.3.2.7.1.3.4). v4.1.3 returned the field value; the return type is unchanged, so code that added 800
  itself now gets a wrong value without a compile error. The field value is available as the new
  `getBarometricPressureSettingEncoded()` (#183)
- `hasTCASResolutionAdvisory()` is now `isCollisionAvoidanceResolutionAdvisoryActive()`, taking version 3's term ("CA RA
  Active") as the unified name, with `isTCASResolutionAdvisoryActive()` available on the version 1 and 2
  layouts under the term those standards use. The same generalization as ME 11, where version 3 renamed
  "TCAS Operational" to "CA Operational"; `isTCASOperational()` is the ED-102A-era accessor on the version 2 airborne
  capability class layouts
- Quality indicators are now reported as types rather than numbers with sentinel values, in the new package
  `decoding.quality`. Every such field is reached two ways: `getXEncoded()` for the category as the standard's table
  numbers it, and an accessor named for the quantity that returns what the category guarantees —
  `getContainmentRadius()` (NIC), `getEstimatedPositionUncertainty()` (NACp), `getHorizontalVelocityError()` (NACv and
  version 0's NUCr), `getSourceIntegrityLevel()` (SIL), `getGeometricVerticalAccuracy()` (GVA) and
  `getSystemDesignAssurance()` (SDA). Each type answers `getGuaranteedUpperBound()` with a single number, `NaN` where
  the category guarantees nothing, so `getGuaranteedUpperBound() < limit` is false whenever the quality is unknown
- Accordingly `getNIC()`, `getNACp()`, `getNACv()` and `getSIL()` are renamed to `getNICEncoded()` and so on, as are
  the categories this release adds. `Encoded` means "the number the standard's table names", whether the message
  transmitted it or the library derived it from the format type code. The exception is the surface capability class,
  whose `getNACv()` returns a category rather than a number and is therefore `getHorizontalVelocityError()`
- Removed the translations these replace: `getPositionUncertainty()`, `getAccuracyBound()`,
  `OperationalStatus.nacPtoEPU()` and `SurfacePosition.decodeEPU()`, each of which returned `-1` for values the
  standards distinguish
- `getHorizontalContainmentRadiusLimit()` returns the containment radius' guaranteed upper bound, which is `NaN`
  rather than `-1` where nothing is guaranteed and `POSITIVE_INFINITY` where the standard bounds the radius only from
  below
- A position message is no longer told its NIC supplements, it is asked with them. The `setNICSupplement{A,C}()`
  setters are gone; the decoder collects what it knows into an immutable `NICSupplements`, which a caller can pass to
  `getNavigationCharacteristics(NICSupplements)` to get the row for any set of supplements, while the no-argument
  `getNICEncoded()` and `getContainmentRadius()` answer from what the decoder knew. `NICSupplement` and
  `NICSupplementD` are three-valued, so "not known" is distinct from "clear" — the two were previously both `false`,
  which made an unknown supplement report the better of the two rows
- TIS-B position messages read their integrity against the version 1 mapping, ED-102B §N.3.2.2 TABLE N-16,
  which is the table the standard has the ground station choose their TYPE Code in line with. Earlier releases read
  the version 0 mapping instead, which grades type codes 7, 11, 13 and 16 without a NIC supplement — at type code 16
  that is NIC 1 where version 1 gives 2 or 3, and at 13 it reported the better of the two rows. `getNACp()`,
  `getPositionUncertainty()` and `getSIL()` are gone from `FineAirbornePositionMsg` and `FineSurfacePositionMsg`, with
  the `AirbornePosition.typeCodeTo…()` helpers behind them: TABLE N-16 has two columns, NIC and containment radius,
  so a TIS-B position message implies neither an accuracy category nor an integrity level
- `TargetStateAndStatusMsg` and the TIS-B velocity messages follow the same naming: `getSIL()` → `getSILEncoded()`,
  `getNACp()` → `getNACpEncoded()`
- New interfaces `SILMsg` and `NACpMsg` carry each field's raw and typed accessor. `SILMsg` also carries
  `getSILSupplement()`, so every message reporting a source integrity level says whether the probability is per
  flight hour or per sample; version 1 answers "per flight hour", which it fixes rather than transmits
- The emergency state is interpreted per ADS-B version, in the new package `decoding.emergency`. Its value meant the
  version 1 and 2 table in every version, although version 3 redefines codes 2, 6 and 7 and version 0 reserves 6.
  `getEmergencyStateCode()` → `getEmergencyStateEncoded()`, and `getEmergencyStateText()` is replaced by
  `getEmergencyState()`, a constant of the transmitting version's enum (`EmergencyStateV0`, `EmergencyStateV1V2` or
  `EmergencyStateV3`) carrying its text. `getReportedEmergencyState()` gives the version 3 value ED-102B Appendix N
  maps it to, which is what to compare across versions. ED-102B gives no mapping for version 2; its codes mean what
  version 1's do, so version 1's mapping is applied
- `EmergencyOrPriorityStatusV0V1Msg` is split into `EmergencyOrPriorityStatusV0Msg` and
  `EmergencyOrPriorityStatusV1Msg`, versions 0 and 1 mapping code 6 differently
- New interface `EmergencyStateMsg` carries the emergency state accessors. `EmergencyOrPriorityStatusMsg` extends it,
  and the version 1 target state and status message implements it, its `getEmergencyPriorityStatus()` becoming
  `getEmergencyStateEncoded()`
- `getAirplaneLength()` and `getAirplaneWidth()` on surface operational status messages are replaced by
  `getAircraftVehicleSize()`, a constant of the transmitting version's table in the new package `decoding.size`:
  `AircraftVehicleSizeV1V2` for ED-102B TABLE N-18 and ED-102A TABLE 2-74, `AircraftVehicleSizeV3` for ED-102B
  TABLE 2-71. Its length and width are each an `Extent`: at most a value, more than a value, or unknown, with
  `getGuaranteedUpperBound()` following the quality indicators' rule. The old accessors returned `-1` for unknown and
  applied the version 1 and 2 table to version 3 as well, reporting its "more than 75 m" as 85 m and its "more than
  80 m" as 90 m. Code 15 of version 3 now reports no length at all: code 14 accepts every length, so an aircraft
  reaches 15 by width alone, whatever TABLE 2-71 prints. Code 15 of versions 1 and 2 reports neither dimension, as it
  is used for anything longer than 85 m or wider than 90 m (ED-102A TABLE 2-74, ICAO Doc 9871 Appendix B), not the
  at most 85 m by 90 m its table prints. `decoding.OperationalStatus` is removed
- BDS registers follow the accessor conventions this release introduces for the ADS-B messages. `BDSRegister.bdsCode`,
  `getBds()`, `setBds()` and `extractBdsCode()` are removed, so a register is told apart with `instanceof`;
  `BDSRegister` is abstract and each register reports its code through `getBDSCode()`, a new immutable `BDSCode` printed
  as Doc 9871 writes it, e.g. "6,0". `CommonUsageGICBCapabilityReport.getCommonUsageGICBCapabilityReport()` is keyed by
  `BDSCode` instead of strings like `"BDS50"`, in table order and unmodifiable. Where a field has a status bit, sign bit
  or a scale, it gets `has…()` for its status, `get…Sign()` for its sign and `get…Encoded()` for the value as
  transmitted, beside the interpreting accessor, whose javadoc says what a negative value means;
  `AircraftIdentification` exposes its encoded identification and digits as `IdentificationMsg` does. Every register's
  `toString()` starts with its code and raw message
- Surface position messages report their ground speed through `getMovement()`, a `Movement` of the transmitting
  version's table in the new package `decoding.movement`, instead of `getGroundSpeed()` and
  `getGroundSpeedResolution()`. The speed is an `Interval` in knots with both ends and their bounds. The old
  accessors applied the version 0/1 table to every version, although versions 2 and 3 exclude the lower end where 0
  and 1 include it, redefine code 2 and quantize codes 3–8 differently, so for these versions every speed was off by
  up to a step (#38). TIS-B uses the version 2/3 table, as ED-102B prescribes
- TIS-B coarse position messages report their ground speed through `getGroundSpeed()`, an `Interval` in knots with
  both ends and their bounds, instead of `getMinGroundSpeed()` and `getMaxGroundSpeed()`. The raw code is
  `getGroundSpeedEncoded()` (#166)
- The difference from barometric altitude is reported through `getDiffBaroAlt()` as a `DiffBaroAlt` of the
  transmitting version's coding in the new package `decoding.diffbaroalt`: an `Interval` in feet with both bounds, a
  value, and whether the code is unknown or undefined. It replaces `getDiffBaroAlt()` returning `Double`,
  `getDiffBaroAltMidpoint()` and `isDiffBaroAltSaturated()`. Version 3 uses ED-102B TABLE 2-27 as revised by Change 1,
  ADS-R version 3 TABLE 2-186, and every other version the 7-bit field, whose code L stands for (L − 1)·25 ft
  rounded. `StatefulModeSDecoder.getDiffBaroAlt()` returns the same type
- Mode S surveillance, Comm-B, all-call and ACAS replies follow the same accessor conventions as the ADS-B messages and
  BDS registers: raw getters of fields the class interprets carry the `…Encoded` suffix, i.e.
  `getFlightStatusEncoded()`, `getUtilityMsgEncoded()`, `getAltitudeEncoded()` replacing `getAltitudeCode()`,
  `getIdentityEncoded()` replacing `getIdentityCode()`, `getReplyInformationEncoded()`,
  `getResolutionAdvisoryComplementEncoded()`, `getCapabilitiesEncoded()` and `getCodeLabelEncoded()`. Every Mode S
  message's `toString()` starts with its class name and then the downlink message
- `ExtendedSquitter` is only the extended squitter envelope, DF=17–19 with `getMessage()`. The format type code moved
  to the new `TypeCodedExtendedSquitter`, which all ADS-B, ADS-R and fine TIS-B messages extend, and which is what
  the decoder returns for a message it doesn't decode further. `CoarsePositionMsg` extends `ExtendedSquitter`, its
  ME field having no format type code
- Target state and status messages (TYPE Code 29, subtype 1) received before the target's ADS-B version is known are
  decoded as `TargetStateAndStatusV3Msg`, as ED-102B §N.1.2 prescribes. v4.1.3 decoded them as the version 2 message
  and dropped them if ME bit 11 was set, a check that belongs to subtype 0: in subtype 1, ME bit 11 is part of the
  selected altitude, so messages with a selected altitude from 16,352 to 32,704 ft or from 49,120 ft were lost. With
  `StatefulModeSDecoder.builder().decodeBeforeVersionKnown(false)`, they are decoded only once the version is known, as
  ED-102A and ED-102B §N.2.5 NOTE 2 require (#207)

### New Features

- Added support for ADS-B v3, including new v3-only message types: HVA Position/Velocity, Wx AIREP (aircraft state,
  alternate weather state, weather state), UAS/RPAS Contingency, and CAS Operational Coordination
- Default decoding of unspecified/newer ADS-B versions as v3
- Adapted the same refactoring and version support to ADS-R and TIS-B decoding
- Added new per-protocol marker interfaces `ADSBMsg`, `ADSRMsg`, `TISBMsg` for identifying a message's decoding
  path via a single `instanceof` check
- Added `NICSupplementBMsg` interface unifying NIC Supplement B handling across ADS-B and ADS-R
- Added `IMFMsg` interface exposing the ICAO Mode A Flag across ADS-R/TIS-B messages that carry it
- Added `BitReader` utility for bit-range and boolean extraction from byte arrays, replacing ad-hoc bit-fiddling
- Added `StatefulModeSDecoder.builder()`, exposing `decodeDf19Adsb`, `tisbV2CompatibilityMode`, and a configurable
  position decoder supplier, alongside the retained no-arg constructor
- Added `SurfaceSystemStatusMsg.getSurfaceSystemStatusEncoded()`, the 48-bit, manufacturer-defined content of the
  message (ED-102B §2.2.3.2.7.4.3.1), which was reachable only by slicing `getMessage()`
- Added `StatefulModeSDecoder.builder().checkParity(boolean)`, enabled by default, see Breaking Changes
- Added `isConsistent()` to `SelectedVerticalIntention` (BDS 4,0), `TrackAndTurn` (BDS 5,0) and `HeadingAndSpeed`
  (BDS 6,0), which carry no code: whether the reserved bits are ZERO and every field whose status bit is ZERO is ZERO
  as well (ICAO Doc 9871 §A.2.1.1, Tables A-2-64, A-2-80 and A-2-96), the rules that tell such a register from
  another one (#165)
- Added `CommBReply`, implemented by `CommBAltitudeReply` and `CommBIdentifyReply`, with one method per register
  the library decodes, e.g. `asDataLinkCapabilityReport()` or `asHeadingAndSpeed()`, which decodes MB as that
  register; for BDS 1,0, it picks the layout as `DataLinkCapabilityReport.decode()` does. For BDS 1,0, 2,0 and 3,0,
  which identify themselves in MB bits 1-8, it throws `BadFormatException` if MB holds another code, as their
  constructors do (#209)
- Added `CommBReply.getAddressAssumingDataParity()`, the aircraft address of a Comm-B reply that carries data parity
  (DP) instead of address parity, for a given register code or a register decoded from the reply: with DP, the
  address the reply reports is the "Modified AA", the aircraft address with its top 8 bits XORed with BDS1 and BDS2
  (ICAO Annex 10 Volume IV §3.1.2.3.2.1.5). Whether a reply carries DP, it does not tell (#153)
- Added `DataLinkCapabilityReportV0V5.getDTESubAddressSupport()`, the DTE sub-address array of reports below
  subnetwork version 6 (#187)
- Added `DataLinkCapabilityReport.getACASType()`, the collision avoidance system the report announces in MB bits
  11-14 as an `ACASType`: TCAS (or another system per the ACAS version), ACAS Xa, or reserved for ACAS III (#208)
- Added `AirAirSurveillanceReply`, implemented by `ShortACAS` and `LongACAS`, with `isAcquisitionReply()` and the
  reply information accessors in one place; `LongACAS` thereby gains `hasVerticalResolutionCapability()` and
  `hasHorizontalResolutionCapability()`. Its `hasResolutionCapability()` tells whether ACAS can issue RAs in any
  dimension, as the Mode S MOPS describe the codes (#161)
- Added the ACAS X fields of RA reports and coordination replies in `ACASXResolutionAdvisory` and
  `ACASXResolutionAdvisoryReport`: the sense, crossing and strength of the vertical RA, the low-level descend inhibit,
  the continuation bit, and the designation and suppression indicators. `ThreatIdentityData` decodes the binary
  threat altitude of the ACAS X layout, with `getAltitude()` giving the center of its 100 ft step and the new
  `getAltitudeInterval()` the step itself
- Added `StatefulModeSDecoder.builder().decodeBeforeVersionKnown(boolean)`, enabled by default, which applies ED-102B
  §N.1.2: before the target's version is known, messages that version 0 does not define are decoded in the format of
  the version that defines them, i.e. the Mode A code, target state and status (subtype 0 as version 1, subtype 1 as
  version 3), the TCAS resolution advisory and the message types version 3 introduced. Disabled, they are decoded
  only once the version is known, as ED-102A prescribes (#207)
- Added saturation flags for the open-ended top codes: `isWestToEastVelocitySaturated()`,
  `isSouthToNorthVelocitySaturated()` and `isVerticalRateSaturated()` on airborne velocity messages,
  `isAirspeedSaturated()` on airspeed and heading messages, and `isSelectedAltitudeSaturated()` on version 3 target
  state and status messages. The top codes mean more than 1021.5 kt (supersonic: 4086 kt), more than 32608 ft/min and
  65456 ft or more; the numeric getters keep returning the next step, which is then only a lower bound
- Added validity checks for 6-bit IA-5 text: `InternationalAlphabet5.isDefined()`, and `hasValidIdentification()`
  on identification messages, `hasValidAircraftIdentification()` on BDS 2,0 and `hasValidAircraftType()` on the Wx
  AIREP aircraft state message. They tell whether every character has a code ICAO Annex 10 Volume IV TABLE 3-8
  defines; the decoded text still shows an undefined code as a space, as before. For BDS 2,0, whose register cannot be
  told from the reply, this is one indication of whether a Comm-B reply carries it
- Added `ModeACodeV1Msg` to handle the V1 Mode A Code format
- Added a common `AirborneVelocityMsg` interface shared by `AirspeedHeadingMsg` and `VelocityOverGroundMsg`,
  across ADS-B, ADS-R and TIS-B
- CPR local decoding is now exposed as public API
- TIS-B velocity messages report NIC supplement A from ME bit 47, which no earlier release decoded, and the decoder
  carries it to that target's position messages — the supplement TABLE N-16 needs, which TIS-B places in the
  velocity message rather than in an operational status message it does not have
- ADS-B v3 supports the second Operational Mode Code layout of surface messages (format selector 1), the first case
  in the standard of one field having more than one layout
- ADS-R and ADS-B share one class per Capability Class and Operational Mode layout wherever the standard defines them
  identically, so ADS-R v3 airborne operational status reports Mode S reply rate limiting, the Collision Avoidance
  Coordination Capability Bits and the remain-well-clear flag
- ADS-B v3 surface operational status reports Mode S reply rate limiting at ME 29, matching the airborne subtype of
  the same version, and identification messages with FTC=1 fall back to an unparsed message, that type code not being
  defined for identification in version 3
- Version 1 target state and status messages (Subtype 0) are decoded, which earlier releases rejected as reserved.
  The ED-129B rules for the selected altitude apply: none is reported when the target altitude capability is 1 or 2,
  and the 10-bit altitude is limited to codes up to 1010, with `hasTargetAltitudeCapability()` available on its own
- `SystemDesignAssurance` reports all three columns the standard gives an SDA: the supported failure condition, the
  probability of an undetected fault causing false or misleading information, and the software and hardware design
  assurance level
- The version 0 airborne velocity messages report a horizontal velocity error, NUCr mapping one for one onto NACv
- The HVA velocity message reports `getContainmentRadius()`, its Position Integrity Category translated through
  `ContainmentRadius.forPIC()`
- The TIS-B velocity messages report their source integrity level as a category, `null` where the message carries the
  barometric altitude difference in its place
- NIC supplement D is captured from the version 3 airborne velocity message and applied to subsequent position
  messages; it was declared and read but never assigned, so version 3 type codes 20 to 22 could not reach their
  better rows
- `hasTargetAltitudeCapability()` on version 1 target state and status messages
- `StatefulModeSDecoder.getDiffBaroAltTimestamp()` gives the time of the report `getDiffBaroAlt()` returns, so that
  the caller can tell whether it is out of date (#195)
- ADS-B messages of TYPE Code 0 that carry a barometric altitude are decoded as airborne position messages of the
  target's version, without a valid position, so that the altitude a target reports after losing its horizontal
  position is no longer lost (ED-102B §2.2.7.1.1). A TYPE Code 0 message without altitude stays undecoded.
  `StatefulModeSDecoder.builder().decodeTypeCodeZero(false)` disables this (#168)

### Improvements

- `Position`: switched to a more efficient and numerically more stable haversine formula
- Avoided unnecessary defensive copies of the immutable payload array and `QualifiedAddress` in copy constructors
- Rewrote the CPR (Compact Position Reporting) algorithms, including correct handling of the southern-hemisphere
  and surface-position edge cases
- `StatefulModeSDecoder`: extracted dedicated internal decode methods per protocol (ADS-B, ADS-R, TIS-B)
- Extracted common `OperationalStatusMsg`/`PositionMsg` interfaces (e.g. `hasTimeFlag()`) shared across versions
- Added `serialVersionUID` to BDS message classes
- Applied consistent legal headers across all Java sources
- Every message class cites the figure defining its own version's format: versions 0 to 2 in ED-102B Appendix N,
  version 3 in §2, and for ADS-R the paragraph giving its deviations alongside the ADS-B figure it deviates from.
  Many classes had cited the version 3 figure whatever their version, and some named no figure at all
- Added `package-info` for the message packages and for `decoding.quality`, recording when a field belongs to the
  shared package and when to a protocol's own
- The javadoc of `AirspeedHeadingMsg.getHeading()` no longer claims the heading to be relative to geographic north. It
  says which north each message reports: always magnetic north for version 0, the horizontal reference direction of
  the operational status message for versions 1 and 2, and the message's own true/magnetic heading type for TIS-B (#47)
- A raw message of an unknown downlink format is rejected about a quarter faster, its reason being built without
  `String.format`. The `BadFormatException` javadoc now says what it is thrown for
- `StatefulModeSDecoder` documents that it is not thread-safe, and the README shows how to decode on several threads:
  one decoder per thread, with each message routed to a decoder by its address

### Bug Fixes

- `StatefulModeSDecoder` no longer shares its per-target state between ADS-B, ADS-R and TIS-B receptions of the same
  address, nor between transponder (DF=17) and non-transponder (DF=18) ADS-B. Previously, an ADS-R operational
  status could set the version used for direct ADS-B, NIC supplement A from a TIS-B velocity message graded ADS-B
  positions (and vice versa), and CPR frames from different sources could be combined in one position decode (ED-102B
  §2.2.10.1.2, §2.2.17.4, §2.2.18.4)
- The ADS-R message classes reject messages other than DF=18 with CF=6 (ED-102B §2.2.18.3) with a
  `BadFormatException`, as the TIS-B classes check their own DF and CF. Built directly from e.g. a DF=17 message, they
  used to return a plausible ADS-R message; the decoder never did that
- `Tools.toHexString(int, int)` reads its input as an unsigned 32-bit value, as `String.format("%x")` does. A value
  with the top bit set produced no digits, so `-1` came out as zeros only
- The stateful position decoder keeps its own copy of the last position as the reference for local CPR decoding.
  It used to keep the very object it returned, so a caller changing the returned latitude or longitude moved the
  reference, and the target's next local decode could come out a latitude zone off
- The two components of a velocity over ground are available on their own, as ED-102B TABLE 2-20 and TABLE 2-22
  code them: `getWestToEastVelocity()` and `getSouthToNorthVelocity()` returned `null` as soon as either component was
  unavailable, and `hasWestToEastVelocity()` and `hasSouthToNorthVelocity()` now tell each apart, while
  `hasVelocity()` still means both. `getTrueTrackAngle()` returns `null` for a target not moving, both components
  being 0 kt, instead of 0°, a track that was never reported
- Malformed hex input is rejected: `Tools.hexStringToByteArray()` throws `IllegalArgumentException` for a string of
  odd length or with a character that is no hex digit, and the `ModeSDownlinkMsg` and `StatefulModeSDecoder.decode()`
  variants taking a hex string throw `BadFormatException` for it, as documented. Previously an odd length threw
  `StringIndexOutOfBoundsException`, and characters other than hex digits were silently converted, so a corrupted line
  was decoded as a message
- `ModeSDownlinkMsg.equals()` and `hashCode()` agree: two messages are equal if their content is, and their parity
  fields are the same or one is the other with the CRC subtracted. Previously, messages that compared equal could hash
  differently, breaking deduplication in hash-based collections, and an intact message compared equal to any copy
  with an arbitrary parity field, e.g. an extended squitter carrying its address as parity
- TIS-B fine surface positions are paired for global CPR decoding only if received within 10 seconds, as ED-102B
  §2.2.17.4.2 requires for TIS-B; they used the ADS-B surface window of 25 or 50 seconds, within which a fast target
  could be decoded a latitude zone, about 170 km, off. `CPREncodedPosition.ofSurface(...)` takes the window as a
  `Duration` for such sources
- Local CPR decoding wraps the longitude into [-180, 180), as global decoding does. Near the antimeridian it could
  return e.g. 180.02° for 179.98°W, which the decoder's own range check then flagged as not reasonable, and which the
  stateful position decoder kept as the reference for the target's following positions
- Target state and status messages with their reserved ME bits 55-56 set are decoded instead of throwing
  `BadFormatException` out of `StatefulModeSDecoder.decode(...)`, in ADS-B and ADS-R alike. ED-102B
  §2.2.3.2.7.1.3.19 only requires transmitters to set the bits to 0; checking them cost every valid field of the
  message, and would have cost all of them once the bits are assigned in a future version
- A TYPE Code 31 message sets the tracked ADS-B version only if it is an operational status: subtype 0, or subtype 1
  from version 1 on. The reserved subtypes 2 to 7 (ED-102B TABLE 2-9, TABLE 2-46) and a surface subtype claiming
  version 0 used to set the version from bits undefined for them, so the target's following messages were decoded with
  a wrong layout until the next operational status, in ADS-B and ADS-R alike
- `ModeSDownlinkMsg.checkParity()` respects `noCRC`: with the CRC already subtracted from the parity field, an intact
  message has a parity field of 0, which the method used to report as a failed check. `ExampleDecoder` leaves the
  parity check to the decoder and no longer works around this
- `StatefulModeSDecoder.getAdsbVersion()` and `getDiffBaroAlt()` no longer create per-target state for the address
  they are asked about, so queries for arbitrary addresses no longer grow the decoder's map without bound, and a query
  no longer keeps an otherwise unused target from being cleaned up
- BDS registers keep their message through Java serialization: `BDSRegister`, which holds it, did not implement
  `Serializable`, so after a round trip `getMessage()` returned `null`, and `toString()` and the resolution advisory
  getters of `ACASActiveResolutionAdvisoryReport` threw `NullPointerException`
- ACAS X resolution advisories are decoded in their own layout, ICAO Annex 10 Volume IV (6th edition)
  §4.3.8.4.2.2.2 and §4.3.8.4.2.4.2.2, which the RA message format in bits 53-54 selects, in RA reports (BDS 3,0), the
  1090ES RA broadcast and coordination replies (DF=16). Earlier releases read them in the TCAS layout: the ARA
  included the LDI and RMF bits, a threat's altitude, range and bearing were misread or reported as no identity data,
  a threat's address was lost, and the report could throw (#133)
- The reply information of the air-air surveillance replies (DF=0, DF=16) is read as ICAO Annex 10 Volume IV
  §4.3.8.4.1.2 and §3.1.2.8.2.2 define it: `hasOperatingACAS()` reported an operating ACAS for every acquisition
  reply (RI 8-15), which any Mode S transponder sends, and for the reserved and not assigned codes 5-7 (#134)
- `AllCallReply` built from a message with `noCRC = true`, whose parity field already holds the interrogator code,
  applied the CRC a second time, so its code label and interrogator code were wrong and `hasValidInterrogatorCode()`
  rejected valid replies (#154)
- `AllCallReply.hasValidInterrogatorCode()` rejects code label 1 with interrogator code 0, which stands for the
  surveillance identifier 0 that ICAO Annex 10 Volume IV §3.1.2.5.2.1.2.4 rules out. The check had been disabled
  because its earlier form also rejected the valid SI codes 16, 32 and 48 (#189)
- RI 1 of the air-air surveillance replies is read as EUROCAE ED-73F (RTCA DO-181F) §3.27.1.5 defines it, an active
  CAS of junior status or a passive CAS, each with resolution capability: `hasOperatingACAS()` reports an operating
  ACAS, and the vertical and horizontal resolution capability are unknown, since the code gives no dimension. ICAO
  Annex 10 Volume IV leaves the code not assigned. It used to report no resolution capability (#161)
- A data link capability report (BDS 1,0) is decoded in the layout its Mode S subnetwork version number selects: from
  version 6 on, the version of DO-181F and ED-73F, MB bits 41-56 hold the basic dataflash and phase overlay
  capabilities, the active transponder side and the change indicator; below it, as ICAO Doc 9871 First and Second
  Edition define them, the support status of DTE sub-addresses 0 to 15. Earlier releases read every report in the
  ED-73F layout, so a supported DTE sub-address could read as one of these capabilities (#187)
- Messages are now actually Java-serializable: `QualifiedAddress` and `CPREncodedPosition` did not implement
  `Serializable`, so writing any message failed with `NotSerializableException`, as it did in earlier versions
- Operational status messages with an unrecognized Capability Class or Operational Mode format selector are no longer
  discarded. The selector governs one field; the rest of the message — MOPS version, NIC supplement A, NACp, SIL and
  the rest — is positionally fixed and now decodes as normal, with the field itself reported as
  `UnknownCapabilityClassCode`/`UnknownOperationalModeCode`. Previously this threw `BadFormatException` and lost the
  message, including the NIC supplements that subsequent position decoding depends on
- Fixed GPS antenna offset decoding in `SurfaceOperationalStatusV2Msg`
- Fixed `MilitaryExtendedSquitter` constructor (AF field is not applicable there)
- Airborne Position messages with FTC=0 now correctly report `AltitudeType.BAROMETRIC_ALTITUDE` (#40)
- TCAS Resolution Advisory (FTC=28, subcode=2) is no longer decoded as such for version 1 targets, where this
  combination is undefined; the message falls back to a plain `TypeCodedExtendedSquitter` (#42). Before the version is
  known, it is still decoded unless `decodeBeforeVersionKnown` is disabled
- `StatefulModeSDecoder` no longer throws on an undefined Operational Status V0 message (#43)
- Fixed decoding of Aircraft Operational Status messages for surface participants (#66)
- Fixed `VelocityOverGroundMsg` decoding for ADS-R
- Geometric vertical accuracy 3, the most accurate category ED-102B defines, was reported as `-1` — the same answer
  as knowing nothing
- Eleven of the sixteen navigation accuracy categories for position, category 0 and the reserved values among them,
  were reported as `-1`
- Surface format type code 8 reported a source integrity level of 2 where ED-102B gives 0, that type code bounding its
  containment radius only from below
- `adsr.SurfacePositionV2Msg` reported a containment radius of `-71.0` metres, a `byte` cast inside a `double`
  expression
- `adsr.AirbornePositionV2Msg` derived the containment radius from NIC supplement A, where version 2 defines
  supplement B, so 926 m was unreachable and accuracy was over-reported at format type code 13
- ADS-R identification messages with FTC=1 are recognized as such, and their IMF flag read
- TIS-B coarse position messages (CF=3) reported the address type the wrong way round: an ICAO 24-bit address, IMF
  clear, as `MODEA_TRACK` and a Mode A code with track file number, IMF set, as `ICAO24`. `getIMF()` itself was
  correct
- `StatefulModeSDecoder.getDiffBaroAlt()` kept returning the last available difference after the target reported none,
  which it does once it has lost geometric or barometric altitude, or, in version 3, with an N/A code. Every report
  now replaces the stored one, so the caller sees its status (#160)
- After a gap of more than 120 seconds, the stateful position decoder decoded a target's position locally against
  its last position, so a target that had moved more than half a CPR zone got a wrong position flagged reasonable.
  It now decodes the target as a new one (#137)
- `StatefulModeSDecoder` removes the targets not seen for more than an hour once every million messages, as
  documented. The counter was never reset, so that after the first million messages, every message scanned all
  targets while more than 30000 were tracked, slowing decoding by about three orders of magnitude, and the counter's
  overflow after 2³¹ messages disabled the cleanup. The cleanup no longer requires more than 30000 targets, so that
  below that number, the state of a target not seen for hours is no longer kept (#140)
- TIS-B coarse position messages gave ground speed code 63 an upper end of 2000 kt, where ED-102A §2.2.17.3.5.7
  Table 2-111 defines it as at least 1968 kt with no upper end (#166)
- `ShortACAS` answered `null` for reply information code 2, "resolution capability inhibited", from
  `hasVerticalResolutionCapability()` and `hasHorizontalResolutionCapability()`; it now answers `false`
- `TrackAndTurn` (BDS 5,0) truncated positive roll angles, true track angles and track angle rates to whole units,
  integer arithmetic dropping their 45/256°, 90/512° and 1/32 °/s resolution, so a track angle rate below 1 °/s read
  as 0. Negative values were unaffected
- `HeadingAndSpeed` (BDS 6,0) truncated positive magnetic headings to whole degrees, integer arithmetic dropping their
  90/512° resolution; negative ones were unaffected
- `ThreatIdentityData.getRange()` divided in integer arithmetic, reporting e.g. −0.05 NM for an encoded 5 where the
  lower bound is 0.35 NM; `getAltitude()`, `getRange()` and `getBearing()` threw a `NullPointerException` for a
  threat identified by its address instead of answering `null`
- Comm-D ELM replies (DF 24) lost their KE bit and the most significant bit of ND to the downlink format
  normalization: `isAck()` was always false, segment numbers 8–15 read as 0–7, and a reply with either bit set was
  given a wrong address, its parity being checked against a first byte rebuilt without them (#85)
- Military extended squitters (DF=19 with AF other than 0) threw `UnspecifiedFormatError` instead of decoding as
  `MilitaryExtendedSquitter`, which now extends `ExtendedSquitter` and reports the application field through
  `getApplicationField()`
- TIS-B/ADS-R Traffic Uplink Management Messages (DF=18 with CF=4) threw `UnspecifiedFormatError` instead of decoding
  as `ManagementMessage`, which now extends `ExtendedSquitter` and reports the Management Message Bit Field through
  `getManagementMessageBitFieldEncoded()` and the service flags of ED-102B TABLE 2-187
- Altitude codes in 100 ft increments (Gillham code) that the ICAO Annex 10 Volume IV table does not contain decode to
  `null`: C1C2C4 = 000, 101 and 111 in every 500 ft band, and 001 and 011 in the lowest band, below the table's
  -1000 ft. Earlier releases decoded them to altitudes, down to -1200 ft, in every message that carries an altitude
  code (#128, #188)
- An RA report (BDS 3,0) or ES TCAS RA broadcast whose threat bearing has one of the unassigned codes 61 to 63 is
  decoded, with `getBearing()` returning `null` as for no bearing estimate (ICAO Annex 10 Volume IV
  §4.3.8.4.2.2.1.6.3). It used to throw `BadFormatException`, which discarded the whole message and, for the ES
  broadcast, escaped `StatefulModeSDecoder.decode()` (#147)
- `ThreatIdentityData.getRange()` returned 0.05 NM both for code 1, "less than 0.05 NM", and code 2, 0.05 to
  0.15 NM, so the two could not be told apart (#186)

Note: this release also includes all fixes from v4.1.3 below, which were made directly on `main` before that
maintenance release was cut from an earlier point in history.

## v4.1.3

### Improvements

- CPR: fixed global decoding for surface positions (southern-hemisphere wrap was only handled for airborne positions)
- `StatefulModeSDecoder` now only decodes positions with a valid FTC, matching `PositionMsg.hasValidPosition()`
- Migrated Maven publishing to Central Portal, ahead of OSSRH's end of life
- Bumped `commons-lang3` from 3.14.0 to 3.18.0

### Bug Fixes

- Fixed vertical rate availability reporting in `VelocityOverGroundMsg`
- Fixed geo-minus-baro and vertical rate availability reporting in `AirspeedHeadingMsg`
- Fixed a `&`/`&&` typo in `Position`'s pole-proximity correction
- Fixed the extended squitter downlink-format range check

## v4.1.2

### Bug Fixes

- Fixed Data Link Capability Change Indication in BDS 1,0
- Fixed TCAS version in BDS 1,0 (DataLinkCapabilityReport)

## v4.1.1

### Improvements

- Improved usability of `QualifiedAddress`
- Performance improvement of int to hex conversion

### Bug Fixes

- Decoding error for horizontal containment radius limit in `SurfacePositionV2Msg`

## v4.1.0

### New Features

- Added option to disable speed-based reasonableness tests for CPR decoding
- Added getter for Q bit

### Improvements

- Limited receiver as reference to surface positions
- Optimized CRC implementation
- Access to `extractBdsCode` method

### Bug Fixes

- Decoding error in identity code of emergency status messages
- Decoding error in DF 24

## v4.0.0

This is the first release of `lib1090` after its fork from [java-adsb](https://github.com/openskynetwork/java-adsb).
The library has undergone a lot of refactoring and cleanup. Despite of all the breaking changes, moving from java-adsb
version `3.X` to lib1090 should not be a large effort.

We have decided to keep the version numbering of the original project. Due to the breaking API changes, we are happy
to release our first version of the library as `v4.0.0`.

Please find an overview of all the changes below.

### Breaking Changes

- Restructured Java packages
- Renamed `ModeSReply` to `ModeSDownlinkMsg`
- Introduced `QualifiedAddress` as aircraft identifier to replace ICAO 24 bit address.
  This allows different types of targets as required by ADS-R/TIS-B.
- Renamed `ModeSDecoder` to `StatefulModeSDecoder`
- Providing a timestamp is now mandatory when decoding messages
- Changed return value of nearly all `toString()` methods
- Renamed `getHeading()` method to `getTrueTrackAngle()` in `VelocityOverGroundMsg`
- Cleaner semantics for `isAirborne()`/`isOnGround()` status in `AllCallReply`, `AltitudeReply`, `CommBAltitudeReply`
  `IdentifyReply` and `CommBIdentifyReply`
- Removed deprecated `Decoder` class

### New Features

- Added ADS-R and TIS-B decoding
- Added decoders for various BDS registers (10, 17, 20, 30, 40, 50, 60)
- Added `hasAlert()` and `hasSPI()` methods to `AirbornePositionMsg`
- Allow custom logic for position decoding
- Introduced altitude type to `Position` (barometric, above ground, ...)

### Bug Fixes

- Fixed longitude bug for surface position messages
- Fixed NIC values for MOPS v0 Airborne Positions

### Misc Changes

- Removed logging and slf4j dependency
