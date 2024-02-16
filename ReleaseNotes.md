![gematik GmbH](docs/Gematik_Logo_Flag.png)

# Release Notes

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