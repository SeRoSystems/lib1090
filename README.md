lib1090
=========

This is a Mode S, ADS-B, TIS-B and ADS-R decoding library for Java. It was forked from the OpenSky Network's (http://www.opensky-network.org) java-adsb library and refactored entirely to accommodate TIS-B and ADS-R.

It is based on these two references:
* ICAO Aeronautical Telecommunications Annex 10 Volume 4 (Surveillance Radar and Collision Avoidance Systems)
* RTCA DO-260B "Minimum Operational Performance Standards (MOPS) for 1090ES"

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

The Comm-B registers, Comm-D data link and military ES are not parsed. Comm-B and D will follow at some point.

The formats are implemented according to RTCA DO-260B, i.e. ADS-B Version 2. The decoder properly takes care of older versions.

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

Get the latest version number [here](https://mvnrepository.com/artifact/de.sero-systems/lib1090).
