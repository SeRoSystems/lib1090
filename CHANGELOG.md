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
- Added a common `AirborneVelocityMessage` interface shared by `AirspeedHeadingMsg` and `VelocityOverGroundMsg`,
  across ADS-B, ADS-R and TIS-B
- CPR local decoding is now exposed as public API

### Improvements
- `Position`: switched to a more efficient and numerically more stable haversine formula
- Avoided unnecessary defensive copies of the immutable payload array and `QualifiedAddress` in copy constructors
- Unified NIC Supplement B handling across ADS-B and ADS-R via the shared interface
- Rewrote the CPR (Compact Position Reporting) algorithms, including correct handling of the southern-hemisphere
  and surface-position edge cases
- `StatefulModeSDecoder`: extracted dedicated internal decode methods per protocol (ADS-B, ADS-R, TIS-B)
- Extracted common `OperationalStatusMsg`/`PositionMsg` interfaces (e.g. `hasTimeFlag()`) shared across versions
- Added `serialVersionUID` to BDS message classes
- Applied consistent legal headers across all Java sources

### Bug Fixes
- Fixed GPS antenna offset decoding in `SurfaceOperationalStatusV2Msg`
- Identification messages with FTC=1 now fall back to an unparsed message under ADS-B v3, since FTC=1 is not
  defined for identification in that version
- Fixed `MilitaryExtendedSquitter` constructor (AF field is not applicable there)
- Airborne Position messages with FTC=0 now correctly report `AltitudeType.BAROMETRIC_ALTITUDE` (#40)
- TCAS Resolution Advisory (FTC=28, subcode=2) is now only decoded as such for V2 targets; for V0/V1 targets, where
  this combination is undefined, the message falls back to a plain `ExtendedSquitter` (#42)
- `StatefulModeSDecoder` no longer throws on an undefined Operational Status V0 message (#43)
- Fixed decoding of Aircraft Operational Status messages for surface participants (#66)
- Fixed `VelocityOverGroundMsg` decoding for ADS-R

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
