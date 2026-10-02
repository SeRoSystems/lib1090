lib1090 [![Maven Central](https://img.shields.io/maven-central/v/de.sero-systems/lib1090.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22de.sero-systems%22%20AND%20a:%22lib1090%22)
=========

This is a Mode S, ADS-B, TIS-B and ADS-R decoding library for Java. It was forked from the OpenSky Network's
(http://www.opensky-network.org) java-adsb library and refactored entirely to accommodate TIS-B and ADS-R.

It is based on these two references:

* ICAO Aeronautical Telecommunications Annex 10 Volume IV (Surveillance Radar and Collision Avoidance Systems)
* RTCA DO-260C / Eurocae ED-102B "Minimum Operational Performance Standards (MOPS) for 1090ES"

It supports the following Mode S downlink formats:

* DF 0: Short air-air ACAS
* DF 4: Short altitude reply
* DF 5: Short identify reply
* DF 11: All-call reply
* DF 16: Long air-air ACAS
* DF 17/18: Extended Squitter (see ADS-B formats below)
* DF 19: Military Extended Squitter
* DF 20: Comm-B altitude reply
* DF 21: Comm-B identify reply
* DF >24: Comm-D Extended Length Message

The following ADS-B formats are supported:

* BDS 0,5: Airborne position messages (including global and local CPR)
* BDS 0,6: Surface position messages (including global and local CPR)
* BDS 0,8: Identification messages
* BDS 0,9: Airborne velocity messages
* BDS 6,1: Aircraft status reports (emergency/priority, TCAS RA)
* BDS 6,2: Target state and status messages
* BDS 6,5: Operational status reports (airborne and surface)
* BDS 6,8: ADS-B Wx AIREP (Subtype=0 "Aircraft State")
* BDS 6,9: ADS-B Wx AIREP (Subtype=1 "Weather State")
* BDS 6,A: ADS-B Wx AIREP (Subtype=2 "Alternate Weather State")
* BDS 6,E: High Velocity and/or Altitude (Subtype=0 “HVA Position”)
* BDS 6,F: High Velocity and/or Altitude (Subtype=1 “HVA Velocity”)

Note: BDS 6,B through BDS 6,D (ADS-B PIREP) messages are not implemented yet.

The formats are implemented according to RTCA DO-260B (ADS-B Version 2) and DO-260C (ADS-B Version 3).
The decoder properly takes care of older versions and defaults to v3 for unspecified/newer versions.

Basic support for the following Comm-B registers is implemented:

* BDS 1,0: Data link capability
* BDS 1,7: Common usage GICB capability
* BDS 2,0: Identification
* BDS 3,0: ACAS resolution advisory
* BDS 4,0: Selected vertical intention
* BDS 5,0: Track and turn report
* BDS 6,0: Heading and speed report

The type of the Comm-B register cannot be inferred from the message itself. As a passive observer, who does not know
the interrogation, some rule-based (or more sophisticated) approach needs to be applied to derive the type and
instantiate the correct decoder class. This has not yet been implemented in the `StatefulModeSDecoder`.
If required, users of this library need to explicitly call the correct Comm-B message decoder.

The Comm-D data link and military ES are not parsed.

### Decoder options

`new StatefulModeSDecoder()` uses the defaults below. To change any of them, use the builder:

```java
StatefulModeSDecoder decoder = StatefulModeSDecoder.builder()
        .checkParity(false)
        .decodeBeforeVersionKnown(false)
        .build();
```

| Option                     | Default                   | Meaning                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|----------------------------|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `checkParity`              | `true`                    | Checks the parity of every extended squitter (DF=17, DF=18, and DF=19 if decoded as ADS-B) and throws a `BadFormatException` if it does not match, before the message can change the target's state. Disable it only if the input has already been checked, e.g. by the receiver: otherwise a corrupted message can change a target's ADS-B version, NIC supplements and CPR frames.                                                                                                                |
| `decodeBeforeVersionKnown` | `true`                    | The decoder learns a target's ADS-B version from its operational status message and assumes version 0 until then. Enabled, messages that version 0 does not define are decoded before the version is known, in the format of the version that defines them, as ED-102B §N.1.2 allows (e.g. target state and status, the TCAS RA broadcast, the Mode A code). Disabled, they are decoded only once the version is known, as ED-102A §N.1.2 and ED-102B §N.2.5 NOTE 2 require. ADS-R is not affected. |
| `decodeTypeCodeZero`       | `true`                    | Decodes ADS-B messages of TYPE Code 0 ("No Position Information") that carry a barometric altitude as airborne position messages without a valid position, so that the altitude of a target that has lost its horizontal position is not lost (ED-102B §2.2.7.1.1). A TYPE Code 0 message without altitude is never decoded further.                                                                                                                                                                |
| `tisbV2CompatibilityMode`  | `true`                    | Decodes TIS-B formats that version 2 used and ED-102B reserves: the coarse airborne position (CF=3), targets addressed by Mode A code and track file number (CF=2 with IMF=1), and the airspeed and heading velocity subtypes 3 and 4.                                                                                                                                                                                                                                                              |
| `decodeDf19Adsb`           | `false`                   | Decodes DF=19 with AF=0 as ADS-B. ED-102B no longer allows it, since such a message is not guaranteed to be in the ADS-B format; enable it only for legacy data that relies on it.                                                                                                                                                                                                                                                                                                                  |
| `positionDecoderSupplier`  | stateful, with speed test | The position decoder for each target. The default decodes CPR globally and locally with its own reasonableness tests, including a speed test. `positionDecoderSupplierDefault(true)` keeps it but disables the speed test, which helps with networks of receivers whose timestamps fluctuate; `positionDecoderSupplier(...)` sets custom logic.                                                                                                                                                     |

### Decoding on several threads

`StatefulModeSDecoder` is not thread-safe. It keeps state per target — the ADS-B version, NIC supplements and the CPR
frames for position decoding — and that state depends on the order in which a target's messages are decoded, so
locking alone would not make concurrent use of one instance meaningful. To decode on several threads, use one decoder
per thread and route each message to a decoder by its address. A target's messages then always reach the same decoder
in the order they were received, and the decoders share no state.

### Known limitations

**Address type of some TIS-B and ADS-R messages.** For DF=18 the target's address type is derived from the ICAO/Mode A
Flag (IMF), and the standard puts that flag at a different bit position in every message type. Some message types it
permits in ADS-R define no IMF field at all — format type code 25 is the clearest case — so their address type simply
cannot be determined. We consider this a defect in the specification rather than something a decoder can work around.

`lib1090` reports `QualifiedAddress.Type.UNKNOWN` for these. Since the decoder keys its per-target state on the
qualified address *including its type and source*, such a message neither contributes to nor reads that state. For
ADS-R, whose
decoding needs the version established by an earlier operational status message, this means the message is not decoded
further and is returned as a plain `TypeCodedExtendedSquitter`.

Affected format type codes are 0, 23, 24, 25, 27 and 30. Apart from 25 these are either reserved or not decoded by this
library in any case, so 25 is the only one where the limitation costs anything today.

### Packaging

This is a Maven project. You can simply generate a jar file with `mvn package`.
All the output can afterwards be found in the `target` directory. There will
be two jar files

* `lib1090-VERSION.jar` contains lib1090, only.
* `lib1090-VERSION-fat.jar` includes lib1090 and all its dependencies.

#### Maven Central

We have also published this project on Maven Central. Just include the following dependency in your project:

```
<dependency>
  <groupId>de.sero-systems</groupId>
  <artifactId>lib1090</artifactId>
  <version>VERSION</version>
</dependency>
```

Get the latest version number [here](https://search.maven.org/artifact/de.sero-systems/lib1090).

### Code style

The code style is pinned in `.editorconfig`, exported from IntelliJ IDEA with its default settings, so that formatting
does not depend on the IDE version at hand. IntelliJ applies every setting of it when reformatting code; other editors
with EditorConfig support pick up at least the general rules: UTF-8, LF line endings, indentation by 4 spaces, lines of
at most 120 characters, no trailing whitespace and a final newline.
