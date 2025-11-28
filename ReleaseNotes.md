<img align="right" width="250" height="47" src="docs/Gematik_Logo_Flag.png"/> <br/>  

# Release Notes gematik Referenzvalidator Plugins

## ISIK3 Basismodul 1.1.0 (2025-11)

### changed
- Updated the FHIR package to 3.1.0

## ISIK3 Dokumentenaustausch 2.0.1 (2025-11)

### changed
- Updated the FHIR package to 3.0.5

## ISIK3 Medikation 1.0.1 (2025-11)

### changed
- Updated the FHIR package to 3.0.6

## ISIK3 Terminplanung 1.1.1 (2025-11)

### changed
- Updated the FHIR package to 3.0.9

## ISIK3 Vitalparameter 1.0.1 (2025-11)

### changed
- Updated the FHIR package to 3.0.7

## PluginBuilder 2.0.1 (2025-11)

### changed
- Updated dependencies and plugins

## KIM-Nachrichten für das E-Rezept 1.2.0 (2025-08)

### added
- new plugin (`erp-servicerequest`) added (cf. [README.md](./README.md))

## ePA Medication 1.4.0 (2025-08)

### added
- Added support for the [1.1.5 FHIR package](https://simplifier.net/packages/de.gematik.epa.medication/1.1.5).

## EEB (VSDM-Ersatzbescheinigung) 1.0 (2025-07)

### added
- Added support for the [1.1.0 FHIR package](https://simplifier.net/packages/de.gematik.elektronische-versicherungsbescheinigung/1.0.1/). Both 1.0.1 and 1.1.0 FHIR packages are now supported by the plugin (cf. [VSDM-Ersatzbescheinigung in README.md](./README.md#VSDM-Ersatzbescheinigung)).

## PluginBuilder 2.0.0 (2025-07)

> **Warning**
> Breaking changes! Please read the release notes carefully before updating.

### changed
- Support for the new plugin description format (configSpecVersion: 2.0), which enables multiple FHIR validation lists in a single plugin. (cf. [README.md](./README.md#Plugin-Definitionsdatei))

### removed
- Support for the old plugin description format (configSpecVersion: 1.0)

## ISIK1 (2025-07)

### removed
- Due to deprecation of the ISIK1 specification, the ISIK1 plugin has been removed.

## EEB (VSDM-Ersatzbescheinigung) 0.2 (2025-06)
- Updated the FHIR package to [1.0.1](https://simplifier.net/packages/de.gematik.elektronische-versicherungsbescheinigung/1.0.1/) 

## ISIK3 Terminplanung 1.1 (2025-04)

### changed
- Codes from the [Fachabteilungsschluessel-erweitert](https://simplifier.net/Basisprofil-DE-R4/Fachabteilungsschluessel-erweitert/~xml) code system were ignored and not validated by the plugin. The issue has been fixed.
- Updated the FHIR package to 2.0.6

## ISIK2 Terminplanung 1.4 (2025-04)

### changed
- Updated the FHIR package to 2.0.6
 
### fixed
- ISIKKalender and ISIKAppointment resources, which contained a specialty code from the [Fachabteilungsschluessel-erweitert](https://simplifier.net/Basisprofil-DE-R4/Fachabteilungsschluessel-erweitert/~xml) code system, failed the validation due to a problem in HAPI with processing of "fragment" code systems. The issue has been temorarily fixed by patching the affected CodeSystem.

## ISIK3 Dokumentenaustausch 2.0 (2025-03)

### changed
- Removed the following packages due to performance issues: `hl7.terminology.r4-4.0.0`, `hl7.terminology.r4-5.0.0`, `hl7.terminology.r4-5.3.0`

## ISIK3 Basismodul 1.0 (2025-02)

### changed
- Updated FHIR Package version from 3.0.5 to 3.0.6

## ISIK3 Terminplanung 1.0 (2025-02)

### changed
- Updated FHIR Package version from 3.0.3 to 3.0.6

## ISIK3 Vitalparameter 1.0 (2025-02)

### changed
- Updated FHIR Package version from 3.0.2 to 3.0.4

## ISIK3 Medikation 1.0 (2025-02)

### changed
- Updated FHIR Package version from 3.0.1 to 3.0.4

## ISIK3 Dokumentenaustausch 1.0 (2025-02)

### changed
- Updated FHIR Package version from 3.0.3 to 3.0.4

## ePA Medication 1.3.0 (2024-11)

### fixed
- The plugin failed to validate the KBV_VS_SFHIR_KBV_NORMGROESSE value set. The issue has been fixed by referencing the corresponding KBV terminology package.

## PluginBuilder 1.2.0 (2024-11)

### changed
- Errors thrown by the underlying HAPI engine during snapshot generation do not stop the plugin generation process anymore (these can be false positives). Instead, they are logged and the process continues.
- Using version 2.6.1 of the reference validator for plugin tests
- Bumping dependencies

## ePA Medication 1.2.0 (2024-10)

### added
- Validator issues INFO validation messages for CodeSystems with missing definitions (cf. [README.md](./README.md#epa-medication))

### changed
- Updated the FHIR package de.gematik.epa.medication from 1.0.2 to [1.0.3](https://simplifier.net/packages/de.gematik.epa.medication/1.0.3)

## ePA Medication 1.1.0 (2024-07)

### changed
- Updated the FHIR packages to 1.0.2
- Ignoring PZN codes during validation

## ePA Basic 1.1.0 (2024-07)

### changed
- Updated the FHIR packages to 1.0.2

## ISIK Stufe 2 - Terminplanung 1.3 (2024-06)

### changed
- Updated the FHIR package to 2.0.5

## PluginBuilder 1.1.1

### added
- Feature to retest an existing plugin using a folder with valid and invalid test resources

### changed
- PluginBuilder now requires a command parameter as first command line argument to specify the action to be performed (build/test)

### fixed
- Package names with suffix (e.g.-rc1) were not handled correctly.
- Patch resources for ValueSets were ignored. Now they are included in the snapshot.
- PluginBuilder produced warnings about untested profiles in case if test resources referenced more than one profile in the meta.profile tag.
- While resolving wildcard package dependencies (e.g. 1.0.x) PluginBuilder could falsely download the latest minor version of the package if one existed on the package server. The behavior is now fixed. PluginBuilder downloads the latest patch version available.
- PluginBuilder failed on processing FHIR packages without no dependencies declared in their package.json manifest file 

## ISIK Stufe 2 - Basismodul 2.1

### changed
- Updated the FHIR package to 2.0.6

## ISIK Stufe 3 - Votalparameter 0.3

### changed
- Updated the FHIR package to 3.0.2

## ISIK Stufe 3 - Terminplanung 0.3

### changed
- Updated the FHIR package to 3.0.3

## ISIK Stufe 3 - Medikation 0.4

### changed
- Updated the FHIR package to 3.0.1

## ISIK Stufe 3 - Dokumentenaustausch 0.4

### changed
- Updated the FHIR package to 3.0.3

## ISIK Stufe 3 - Basismodul 0.5

### changed
- Updated the FHIR package to 3.0.5

## ISIK Stufe 2 - Terminplanung 1.2

### changed
- Updated the FHIR package to 2.0.4

## ISIK Stufe 1 - Basismodul 2.1

### changed
- Updated the FHIR package to 1.0.10

## ISIK Stufe 3 - Medikation 0.3

### changed
- Excluded the CodeSystem http://fhir.de/CodeSystem/ifa/pzn from validation

## ISIK Stufe 2 - Medikation 2.0

### changed
- Excluded the CodeSystem http://fhir.de/CodeSystem/ifa/pzn from validation

## ISIK Stufe 1 - Basismodul 2.0

### added
- added KBV value sets to be used while validating instances ([Issue 6](https://github.com/gematik/app-referencevalidator-plugins/issues/6))

## ISIK Stufe 2 - Basismodul 2.0

### added
- added KBV value sets to be used while validating instances ([Issue 6](https://github.com/gematik/app-referencevalidator-plugins/issues/6))

## ISIK Stufe 3 - Basismodul 0.3

### added
- added KBV value sets to be used while validating instances ([Issue 6](https://github.com/gematik/app-referencevalidator-plugins/issues/6))
- updated version of the de.gematik.isik-basismodul dependency to 3.0.3

## PluginBuilder 1.0.1

### fixed
- fixed a bug during snapshot generation if dependencies habe upper case characters in their name

## PluginBuilder 1.0.0

### added
- PluginBuilder as a tool for creating new validation plugins
- Modules ISIK1, ISIK2, ISIK3, ISIP1, DIGA have been moved from the Gematik Referenzvalidator project and packaged as plugins
- Module EEB (VSDM-Ersatzbescheinigung)