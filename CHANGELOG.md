# Changelog for lib1090

## v5.0.0

This release is a major refactoring of the message class hierarchy and adds support for ADS-B v3, along with
matching updates to ADS-R and TIS-B decoding. It also folds in a series of correctness fixes to Operational Status
and CPR decoding made since v4.1.3.

### Breaking Changes
- Message classes moved from deep per-version inheritance chains to flat sibling classes implementing shared
  interfaces (new `msgs.squitter` package); code relying on e.g. `AirbornePositionV2Msg instanceof AirbornePositionV1Msg`
  will need to be adapted
- Removed `ModeSDownlinkMsg.subtype` enum and all per-class `getType()` overrides; message identity is now
  determined via `instanceof`/`getClass()`
- Message classes now store raw encoded values and interpret them only through accessors, instead of storing
  interpreted fields directly
- `StatefulModeSDecoder.decode(...)` now takes a mandatory, non-null `java.time.Instant` timestamp instead of `long`
- DF=19/AF=0 is no longer decoded as ADS-B by default; enable via `StatefulModeSDecoder.builder().decodeDf19Adsb(true)`
- Reassigned `serialVersionUID`s across message classes, breaking Java-serialization compatibility with objects
  serialized under earlier versions
- `QualifiedAddress` is no longer a nested class of `ModeSDownlinkMsg`; it now lives at the package level
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
- `hasTCASResolutionAdvisory()` is now `isCollisionAvoidanceResolutionAdvisoryActive()`, taking version 3's term
  ("CA RA Active") as the unified name, with `isTCASResolutionAdvisoryActive()` available on the version 1 and 2
  layouts under the term those standards use. The same generalization as ME 11, where version 3 renamed
  "TCAS Operational" to "CA Operational"; `isTCASOperational()` is the DO-260B-era accessor on the version 2 airborne
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
  reaches 15 by width alone, whatever TABLE 2-71 prints. `decoding.OperationalStatus` is removed
- BDS registers follow the conventions of the ADS-B messages. `BDSRegister.bdsCode`, `getBds()`, `setBds()` and
  `extractBdsCode()` are removed, so a register is told apart with `instanceof`; `BDSRegister` is abstract and each
  register reports its code through `getBDSCode()`, a new immutable `BDSCode` printed as Doc 9871 writes it, e.g.
  "6,0". `CommonUsageGICBCapabilityReport.getCommonUsageGICBCapabilityReport()` is keyed by `BDSCode` instead of
  strings like `"BDS50"`, in table order and unmodifiable. Where a field has a status bit, sign bit or a scale, it gets
  `has…()` for its status, `get…Sign()` for its sign and `get…Encoded()` for the value as transmitted, beside the
  interpreting accessor, whose javadoc says what a negative value means; `AircraftIdentification` exposes its encoded
  identification and digits as `IdentificationMsg` does. Every register's `toString()` starts with its code and raw
  message
- Surface position messages report their ground speed through `getMovement()`, a `Movement` of the transmitting
  version's table in the new package `decoding.movement`, instead of `getGroundSpeed()` and
  `getGroundSpeedResolution()`. The speed is an `Interval` in knots with both ends and their bounds. The old
  accessors applied the version 0/1 table to every version, although versions 2 and 3 exclude the lower end where 0
  and 1 include it, redefine code 2 and quantize codes 3–8 differently, so for these versions every speed was off by
  up to a step (#38). TIS-B uses the version 2/3 table, as ED-102B prescribes
- The difference from barometric altitude is reported through `getDiffBaroAlt()` as a `DiffBaroAlt` of the
  transmitting version's coding in the new package `decoding.diffbaroalt`: an `Interval` in feet with both bounds, a
  value, and whether the code is unknown or undefined. It replaces `getDiffBaroAlt()` returning `Double`,
  `getDiffBaroAltMidpoint()` and `isDiffBaroAltSaturated()`. Version 3 uses ED-102B TABLE 2-27 as revised by Change 1,
  ADS-R version 3 TABLE 2-186, and every other version the 7-bit field, whose code L stands for (L − 1)·25 ft
  rounded. `StatefulModeSDecoder.getDiffBaroAlt()` returns the same type

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

### Bug Fixes
- Operational status messages with an unrecognized Capability Class or Operational Mode format selector are no longer
  discarded. The selector governs one field; the rest of the message — MOPS version, NIC supplement A, NACp, SIL and
  the rest — is positionally fixed and now decodes as normal, with the field itself reported as
  `UnknownCapabilityClassCode`/`UnknownOperationalModeCode`. Previously this threw `BadFormatException` and lost the
  message, including the NIC supplements that subsequent position decoding depends on
- Fixed GPS antenna offset decoding in `SurfaceOperationalStatusV2Msg`
- Fixed `MilitaryExtendedSquitter` constructor (AF field is not applicable there)
- Airborne Position messages with FTC=0 now correctly report `AltitudeType.BAROMETRIC_ALTITUDE` (#40)
- TCAS Resolution Advisory (FTC=28, subcode=2) is now only decoded as such for V2 targets; for V0/V1 targets, where
  this combination is undefined, the message falls back to a plain `ExtendedSquitter` (#42)
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
