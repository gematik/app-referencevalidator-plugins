In diesem Projekt sind Plugins für den [gematik Referenzvalidator](https://github.com/gematik/app-referencevalidator) und ein Werkzeug enthalten, mit dem eigene Plugins erstellt werden können. 

Siehe auch [Release Notes](ReleaseNotes.md).

# Verfügbare Plugins

Die Plugins können unter [Releases](/releases) heruntergeladen werden. 

| **Plugin**                                                                                  | **Version** | **ID**                    |
|---------------------------------------------------------------------------------------------|-------------|---------------------------|
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 1 (Modul Basis)               | 1.1         | isik1                     |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 2 (Modul Basis)               | 1.1         | isik2-basismodul          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 2 (Modul Terminplanung)       | 1.1         | isik2-terminplanung       |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 2 (Modul Vitalparameter)      | 1.1         | isik2-vitalparameter      |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 2 (Modul Medikation)          | 1.1         | isik2-medikation          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Basis)               | 0.2         | isik3-basismodul          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Terminplanung)       | 0.2         | isik3-terminplanung       |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Vitalparameter)      | 0.2         | isik3-vitalparameter      |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Medikation)          | 0.2         | isik3-medikation          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Dokumentenaustausch) | 0.2         | isik3-dokumentenaustausch |
| Informationstechnische Systeme in der Pflege (ISIP) Stufe 1                                 | 1.1         | isip1                     |
| DiGA Toolkit                                                                                | 0.10        | diga                      |
| VSDM-Ersatzbescheinigung                                                                    | 0.1         | eeb                       |

Die Bezeichnung in der Spalte `ID` dient dem Aufruf des Plugins aus der Referenzvalidator-Konsolenanwendung.

## Besonderheiten der Plugins

### ISIP1

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots

### ISIK3

Abweichend vom allgemeinen Prüfumfang verhalten sich die ISIK3-Plugins wie folgt:
- Codes aus den CodeSystemen `http://snomed.info/sct`, `http://fhir.de/CodeSystem/bfarm/icd-10-gm`, `http://fhir.de/CodeSystem/bfarm/atc` und `http://fhir.de/CodeSystem/bfarm/ops` werden nicht validiert
- Folgende ValueSets werden nicht validiert: `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/ProzedurenCodesSCT`, `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/DiagnosesSCT`, `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/ProzedurenKategorieSCT`, `https://gematik.de/fhir/isik/v3/Terminplanung/ValueSet/ISiKTerminPriority`, `https://gematik.de/fhir/isik/v3/Medikation/ValueSet/SctRouteOfAdministration` und `http://fhir.de/ValueSet/bfarm/ops`

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots

### ISIK2

Abweichend vom allgemeinen Prüfumfang verhalten sich die ISIK2-Plugins wie folgt:
- Codes aus den CodeSystemen `http://snomed.info/sct`, `http://fhir.de/CodeSystem/bfarm/icd-10-gm`, `http://fhir.de/CodeSystem/bfarm/atc` und `http://fhir.de/CodeSystem/bfarm/ops` werden nicht validiert
- Folgende ValueSets werden nicht validiert: `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/ProzedurenCodesSCT`, `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/DiagnosesSCT`, `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/ProzedurenKategorieSCT`, `https://gematik.de/fhir/isik/v2/Terminplanung/ValueSet/ISiKTerminPriority`, `https://gematik.de/fhir/isik/v2/Medikation/ValueSet/SctRouteOfAdministration` und `http://fhir.de/ValueSet/bfarm/ops`

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots

### ISIK1

Abweichend vom allgemeinen Prüfumfang verhält sich das ISIK1-Plugin wie folgt:
- Codes aus den CodeSystemen `http://snomed.info/sct`, `http://fhir.de/CodeSystem/bfarm/icd-10-gm` und `http://fhir.de/CodeSystem/bfarm/ops` werden nicht validiert
- Folgende ValueSets werden nicht validiert: `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/ProzedurenCodesSCT`, `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/DiagnosesSCT`, `https://gematik.de/fhir/isik/v2/Basismodul/ValueSet/ProzedurenKategorieSCT` und `http://fhir.de/ValueSet/bfarm/ops`

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots

### DIGA

Abweichend vom allgemeinen Prüfumfang verhält sich das DIGA-Plugin wie folgt:
- Codes aus den CodeSystemen `http://fhir.de/CodeSystem/ifa/pzn` und `http://fhir.de/CodeSystem/dimdi/atc` werden nicht validiert
- Instanzen mit unbekannten Profilen führen zum invaliden Ergebnis
- Instanzen mit unbekannten Extensions führen zum invaliden Ergebnis

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots

### VSDM-Ersatzbescheinigung

#### Anpassungen der Packages:
- Alle Packages enthalten Snapshots
- ValueSet-versicherungsart-de-basis.json in de.basisprofil.r4-1.3.2 korrigiert

# Nutzung mit dem Referenzvalidator

Siehe Dokumentation vom [gematik Referenzvalidator](https://github.com/gematik/app-referencevalidator).

# Erstellung von Plugins mittels Plugin-Builders

## Vorbereitungen
1. [Die letzte Version](/releases/latest) vom PluginBuilder herunterladen
2. Die Ordnerstruktur für das neue Plugin (z.B. mit dem Namen `my-plugin`) vorbereiten:

```
my-plugin/
├── test-files/
│   ├── valid/      
│   └── invalid/
├── src-package/
├── patches/
└── config.yaml
```

3. Die Plugin-Definitionsdatei erstellen (siehe [Plugin-Definitionsdatei](#plugin-definitionsdatei-configyaml)).
4. Test-Instanzen vorbereiten (siehe [Test-Instanzen](#testinstanzen))
5. (optional) Quell-FHIR-Packages ablegen (siehe [Quell-FHIR-Packages](#quell-fhir-packages))
6. (optional) Patches für die Ressourcen in den Quell-FHIR-Packages ablegen (siehe [Patches für die Quell-FHIR-Packages](#patches-für-die-quell-fhir-packages))

### Plugin-Definitionsdatei (`config.yaml`)

| Eigenschaft                                     | Bezeichnung in config.yaml                  | Pflichtangabe | Beschreibung                                                                                                                                                                                                                     | Beispiel                                                                                                                                                                                          |
|-------------------------------------------------|---------------------------------------------|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Version der Definition-Beschreibungssprache     | configSpecVersion                           | Ja            | Derzeit fest auf 1.0                                                                                                                                                                                                             | 1.0                                                                                                                                                                                               |
| Name                                            | id                                          | Ja            | Identifikator des Plugins. Wird für die Paketierung und den Aufruf im Referenzvalidator verwendet                                                                                                                                | isik3-terminplanung                                                                                                                                                                               |
| Version                                         | version                                     | Ja            | Version des Plugins                                                                                                                                                                                                              | 1.0.0                                                                                                                                                                                             |
| Author                                          | author                                      | Ja            | Author des Plugins.                                                                                                                                                                                                              | gematik                                                                                                                                                                                           |
| Beschreibung                                    | description                                 | Ja            | Eine kurze Beschreibung des Plugins und seines Validierungsumfangs.                                                                                                                                                              | Dies ist eine Beispielbeschreibung des Plugins.                                                                                                                                                   |
| Link zur Spezifikation                          | specUrl                                     | Nein          | Link zur Spezifikation des validation FHIR-Packages (validation.fhirPackage)                                                                                                                                                     | https://simplifier.net/packages/de.gematik.isik-terminplanung/3.0.0                                                                                                                                                                                 |
| Paketname                                       | validation.fhirPackage                      | Ja            | Name und Version des FHIR-Packages, dessen Profile vom Plugin validiert werden sollen (Format abc#X.Y.Z)                                                                                                                         | kbv.ita.erp#1.1.0                                                                                                                                                                                 |
| Validierungsabhängigkeiten                      | validation.dependencies                     | Nein          | Liste von FHIR-Packages (Format abc#X.Y.Z), die für die Validierung benötigt werden (falls abweichend von der package.json des Packages unter `validation.fhirPackage`                                                           | - "kbv.basis#1.3.1"<br/>- "kbv.ita.for#1.3.1"                                                                                                                                                     |
| Zu ignorierende Codesysteme                     | validation.ignoredCodeSystems               | Nein          | CodeSysteme, die im Rahmen der Validierung nicht überprüft werden sollen                                                                                                                                                         | "http://fhir.de/CodeSystem/bfarm/icd-10-gm"                                                                                                                                                       |
| Zu ignorierende ValueSets                       | validation.ignoredValueSets                 | Nein          | ValueSets, die im Rahmen der Validierung nicht überprüft werden sollen                                                                                                                                                           | "http://fhir.de/ValueSet/bfarm/ops"                                                                                                                                                               |
| Unbekannte Extensions führen zu Fehlern         | validation.anyExtensionsAllowed             | Nein          | Festlegung, ob unbekannte Extensions in den Instanzen zu Fehlern führen sollen                                                                                                                                                   | false                                                                                                                                                                                             |
| Unbekannte Profile führen zu Fehlern            | validation.errorForUnknownProfiles          | Nein          | Festlegung, ob unbekannte Profile bei meta.profile-Angaben in den Instanzen zu Fehlern führen sollen                                                                                                                             | true                                                                                                                                                                                              |
| Akzeptierte Dateiformate                        | validation.acceptedEncodings                | Nein          | Festlegung, welche Dateiformate bei Validierung ausschließlich akzeptiert werden sollen                                                                                                                                          | xml                                                                                                                                                                                               |
| Kritikalitätsbewertung der Validierungsausgaben | validation.validationMessageTransformations | Nein          | Veränderung der Kritikalitätsstufe der Ausgaben des Validators.                                                                                                                                                                  | transformation1:<br>- severityLevelFrom: "error"<br>- severityLevelTo: "information"<br>- locatorString: "Appointment.participant:AkteurPatient"<br>- messageId: "Validation_VAL_Profile_Minimum" |
| Snapshot-Abhängigkeiten                         | snapshotDependencies                        | Nein          | Liste von zusätzlichen FHIR-Packages (Format abc#X.Y.Z), die für die Snapshot-Generierung benötigt werden. Die Packages werden von [öffentlichen Package-Servern](https://build.fhir.org/packages.html#2.1.10.6) heruntergeladen | "ein-nur-fuer-snapshot-generierung-benoetigtes-package#x.y.z"                                                                                                                                     |

Beispiel einer `config.yaml`-Datei:

```yaml
configSpecVersion: "1.0"
id: "isik3-terminplanung"
version: "1.0.0"
author: "gematik"
description: "Dies ist eine Beispielbeschreibung des Plugins."
specUrl: "https://simplifier.net/packages/de.gematik.isik-terminplanung/3.0.0"
validation:
  fhirPackage: "de.gematik.isik-terminplanung#3.0.0"
  dependencies:
    - "kbv.basis#1.3.1"
    - "kbv.ita.for#1.3.1"
  errorOnUnknownProfile: "true"
  anyExtensionsAllowed: "false"
  acceptedEncodings:
    - "xml"
  validationMessageTransformations:
    transformation1:
      severityLevelFrom: "error"
      severityLevelTo: "information"
      locatorString: "Appointment.participant:AkteurPatient"
      messageId: "Validation_VAL_Profile_Minimum"
  ignoredCodeSystems:
    - "http://snomed.info/sct"
  ignoredValueSets:
    - "http://fhir.de/ValueSet/bfarm/ops"
snapshotDependencies:
  - "ein-nur-fuer-snapshot-generierung-benoetigtes-package#x.y.z"
```

### Testinstanzen

Im Ordner `test-files` müssen mindestens eine valide (im Unterordner `valid`) sowie eine invalide (im Unterordner `invalid`) FHIR-Ressource als Testinstanz von einem Profil aus `validation.fhirPackage` hinterlegt werden, um sicherzustellen, dass die Validierung mit dem gebauten Plugin auch funktioniert. Im besten Fall sollte für jedes enthaltene Profil aus `validation.fhirPackage` jeweils eine valide und eine invalide FHIR-Ressource als Testinstanz hinterlegt werden. Der PluginBuilder weist in seiner Konsolenausgabe auf die Profile mit fehlenden Testinstanzen hin.  

### Quell-FHIR-Packages

Im Ordner `src-packages` können FHIR-Packages im [FHIR Package Standard](https://confluence.hl7.org/display/FHIR/NPM+Package+Specification) hinterlegt werden. Die hier hinterlegten FHIR-Packages werden zusätzlich zu den Abhängigkeiten des Plugin-Pakets (siehe `validation.fhirPackage`) für die Validaierung von FHIR-Ressourcen verwendet.

### Patches für die Quell-FHIR-Packages
Hier können einzelne FHIR-Ressourcen hinterlegt werden, die die FHIR-Ressourcen in originellen FHIR-Packages ergänzen oder ersetzen sollten (z.B. für den Fall wenn in einem Profil eines bereits veröffentlichten Quell-FHIR-Package ein Fehler vorliegt, der die Validierung verhindert). 

** Warning ** 
> Die Dateien müssen exakt denselben Namen haben, wie die fehlerhaften Dateien im Quell-FHIR-Package. Sie müssen im Unterordner abgelegt werden, dessen Name im Format [FHIR-Package-Name]-[FHIR-Package-Version] dem Quell-FHIR-Package entspricht  

Beispiel:

```
my-plugin/
└── patches/
    └── package1-1.0.0/
        └── some-profile-with-fixes.json
```

## Aufruf des Plugin-Builders zur Erstellung von Plugins

> **Warning**
> Um Abhängigkeiten der FHIR-Pakages zu herunterladen, erfordert der Plugin-Builder direkte Verbindung zum Internet. Derzeit ist keine Proxy-Server-Konfiguration möglich

Beispielaufrufe des Plugin-Builders:

`java -jar plugin-builder-cli-X.Y.Z.jar C:\user\plugindefinitions\my-plugin`

`java -jar plugin-builder-cli-X.Y.Z.jar C:\user\plugindefinitions\my-plugin C:\output`

Das erste Argument ist verpflichtend und gibt den Pfad zum Ordner mit Plugin-Ressourcen an. Optional kann als zweite Argument ein Pfad zu einem Ordner angegeben werden, wo das fertig gebaute Plugin abgelegt werden soll. Falls das Ausgabeverzeichnis nicht definiert ist, wird das fertige Plugin neben dem Plugin-Ressourcen-Ordner abgelegt.

Neben Konsolenausgabe schreibt der Plugin-Builder während der Ausführung ein Protokoll mit Warnungen (`warn.log`).

## Bauen von Plugins mit Hilfe von Maven
Alle bereits integrierten Plugins können mit dem Aufruf `mvn clean compile -Pbuild-plugins` gebaut werden. Um ein einzelnes, spezielles Plugin zu bauen wird folgender Aufruf benötigt: `mvn clean compile -pl :valmodule-eeb -Pbuild-plugins`. Dieser Aufruf würde das `valmodule-eeb` einzeln bauen.