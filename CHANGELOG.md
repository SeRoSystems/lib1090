# Changelog for lib1090

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
