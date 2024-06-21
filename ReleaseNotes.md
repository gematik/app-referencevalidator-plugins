<img align="right" width="250" height="47" src="docs/Gematik_Logo_Flag.png"/> <br/>  

# Release Notes gematik Referenzvalidator Plugins

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