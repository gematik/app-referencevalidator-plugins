<img align="right" width="250" height="47" src="docs/Gematik_Logo_Flag.png"/> <br/> 

# gematik Referenzvalidator Plugins

<details>
  <summary>Inhaltsverzeichnis</summary>
  <ol>
    <li><a href="#über-das-projekt">Über das Projekt</a></li>
    <li><a href="#verfügbare-plugins">Verfügbare Plugins</a></li>
    <li><a href="#nutzung-mit-dem-referenzvalidator">Nutzung mit dem Referenzvalidator</a></li>
    <li><a href="#erstellung-von-plugins-mittels-plugin-builders">Erstellung von Plugins mittels Plugin-Builders</a></li>
    <li><a href="#beiträge-zum-projekt">Beiträge zum Projekt</a></li>
    <li><a href="#lizenz">Lizenz</a></li>
    <li><a href="#kontakt">Kontakt</a></li>
  </ol>
</details>

## Über das Projekt

In diesem Projekt sind Plugins für den [gematik Referenzvalidator](https://github.com/gematik/app-referencevalidator)
und ein Werkzeug (PluginBuilder) enthalten, mit dem eigene Plugins erstellt werden können.

### Release Notes

Siehe [ReleaseNotes.md](./ReleaseNotes.md)

## Verfügbare Plugins

Die Plugins können unter [Releases](https://github.com/gematik/app-referencevalidator-plugins/releases) heruntergeladen
werden.

| **Plugin**                                                                                       | **Version** | **ID**                    |
|--------------------------------------------------------------------------------------------------|-------------|---------------------------|
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Basis)                    | 1.1.1       | isik3-basismodul          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Terminplanung)            | 1.2.0       | isik3-terminplanung       |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Vitalparameter)           | 1.1.0       | isik3-vitalparameter      |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Medikation)               | 1.1.0       | isik3-medikation          |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 3 (Modul Dokumentenaustausch)      | 2.1.0       | isik3-dokumentenaustausch |
| Informationstechnische Systeme in Krankenhäusern (ISIK) Stufe 5 (Unified)                        | 1.0.1       | isik5                     |
| Informationstechnische Systeme in der Pflege (ISIP) Stufe 1                                      | 1.1         | isip1                     |
| DiGA Toolkit                                                                                     | 0.10        | diga                      |
| VSDM-Ersatzbescheinigung                                                                         | 1.0         | eeb                       |
| Elektronische Patientenakte Basisfunktionalitäten                                                | 1.1.0       | epa3-basic                |
| Elektronische Patientenakte Medication                                                           | 1.4.0       | epa3-medication           |
| [KIM-Nachrichten für das E-Rezept](https://gemspec.gematik.de/ig/fhir/erp-servicerequest/1.2.0/) | 1.2.0       | erp-servicerequest        |

Die Bezeichnung in der Spalte `ID` dient dem Aufruf des Plugins aus der Referenzvalidator-Konsolenanwendung.

### Besonderheiten der Plugins

#### EPA-Medication

Abweichend vom allgemeinen Prüfumfang verhält sich das EPA-Medication-Plugin wie folgt:

- Codes aus den folgenden Codesystemen werden nicht validiert:
    - `http://fhir.de/CodeSystem/bfarm/atc`
    - `http://fhir.de/CodeSystem/ask`
    - `http://fhir.de/CodeSystem/ifa/pzn`
    - "http://snomed.info/sct"
    - "https://terminologieserver.bfarm.de/fhir/CodeSystem/arzneimittel-referenzdaten-pharmazeutisches-produkt"

_Zusatzinformation:_ Das Plugin lädt derzeit zwei große HL7-Terminologiepakete (je ~5 MB), was die Paketgröße und damit
die Validierungsdauer deutlich erhöht, obwohl vermutlich nur wenige enthaltene ValueSets/CodeSystems tatsächlich
benötigt werden; eine Lösung ist geplant, hat aber aktuell keine Priorität.

#### ISIK5

Abweichend vom allgemeinen Prüfumfang verhaltet sich das Plugin wie folgt:

- Codes aus den CodeSystemen `http://snomed.info/sct`, `http://fhir.de/CodeSystem/bfarm/icd-10-gm`,
  `http://fhir.de/CodeSystem/bfarm/atc`, `http://fhir.de/CodeSystem/ifa/pzn`, `http://fhir.de/CodeSystem/bfarm/ops` und
  `http://dvmd.de/fhir/CodeSystem/kdl` werden nicht validiert
- Folgende ValueSets werden nicht validiert: `https://gematik.de/fhir/isik/ValueSet/ProzedurenCodesSCT`,
  `https://gematik.de/fhir/isik/ValueSet/DiagnosesSCT`,
  `https://gematik.de/fhir/isik/ValueSet/ProzedurenKategorieSCT`,
  `https://gematik.de/fhir/isik/ValueSet/ISiKTerminPriority`,
  `https://gematik.de/fhir/isik/ValueSet/SctRouteOfAdministration`, `http://fhir.de/ValueSet/bfarm/ops` und
  `http://dvmd.de/fhir/ValueSet/kdl`

#### ISIK3

Abweichend vom allgemeinen Prüfumfang verhalten sich die ISIK3-Plugins wie folgt:

- Codes aus den CodeSystemen `http://snomed.info/sct`, `http://fhir.de/CodeSystem/bfarm/icd-10-gm`,
  `http://fhir.de/CodeSystem/bfarm/atc`, `http://fhir.de/CodeSystem/ifa/pzn` und `http://fhir.de/CodeSystem/bfarm/ops`
  werden nicht validiert
- Folgende ValueSets werden nicht validiert: `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/ProzedurenCodesSCT`,
  `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/DiagnosesSCT`,
  `https://gematik.de/fhir/isik/v3/Basismodul/ValueSet/ProzedurenKategorieSCT`,
  `https://gematik.de/fhir/isik/v3/Terminplanung/ValueSet/ISiKTerminPriority`,
  `https://gematik.de/fhir/isik/v3/Medikation/ValueSet/SctRouteOfAdministration` und `http://fhir.de/ValueSet/bfarm/ops`
- Validierung ausgewählter [KBV-Schlüsseltabellen](https://applications.kbv.de/overview.xhtml), siehe Package
  gematik.kbv.sfhir.cs.vs im Plugin
- Dokumentenaustausch: Aus Performancegründen werden derzeit keine Codes und ValueSets aus dem Terminologiepackage
  `hl7.terminology.r4` validiert

#### DIGA

Abweichend vom allgemeinen Prüfumfang verhält sich das DIGA-Plugin wie folgt:

- Codes aus den CodeSystemen `http://fhir.de/CodeSystem/ifa/pzn` und `http://fhir.de/CodeSystem/dimdi/atc` werden nicht
  validiert
- Instanzen mit unbekannten Profilen führen zum invaliden Ergebnis
- Instanzen mit unbekannten Extensions führen zum invaliden Ergebnis

#### VSDM-Ersatzbescheinigung

Mit dem Modul können sowohl die Instanzen
der [1.0](https://simplifier.net/packages/de.gematik.elektronische-versicherungsbescheinigung/1.0.1)- als auch
der [1.1-Spezifikation](https://simplifier.net/packages/de.gematik.elektronische-versicherungsbescheinigung/1.1.0/)
validiert werden.
Die [Übergangszeiträume für die Gültigkeit der Spezifikationen](https://simplifier.net/guide/Implementierungsleitfaden-VSDM-Ersatzbescheinigung/Einfuehrung/Interoperabilitaetsvorgaben?version=current)
werden vom Plugin berücksichtigt.

Anpassungen der Packages:

- ValueSet-versicherungsart-de-basis.json in de.basisprofil.r4-1.3.2 korrigiert

#### KIM-Nachrichten für das E-Rezept

Abweichend vom allgemeinen Prüfumfang verhält sich das Plugin wie folgt:

- Codes aus den folgenden Codesystemen werden nicht validiert:
    - `http://fhir.de/CodeSystem/bfarm/atc`
    - `http://fhir.de/CodeSystem/ask`
    - `http://fhir.de/CodeSystem/ifa/pzn`
    - "http://snomed.info/sct"
    - "https://terminologieserver.bfarm.de/fhir/CodeSystem/arzneimittel-referenzdaten-pharmazeutisches-produkt"

Zur erfolgreichen Validierung muss zusätzlich die Abgängikeit `kbv.all.st#1.24.0` eingebunden werden.

## Nutzung mit dem Referenzvalidator

Siehe Dokumentation vom [gematik Referenzvalidator](https://github.com/gematik/app-referencevalidator).

## Erstellung von Plugins mittels Plugin-Builders

### Vorbereitungen

1. [Die letzte Version](https://github.com/gematik/app-referencevalidator-plugins/releases/latest) vom PluginBuilder
   herunterladen
2. Die Ordnerstruktur für das neue Plugin (z.B. mit dem Namen `my-plugin`) vorbereiten:

```
my-plugin/
├── test-files/
│   ├── valid/      
│   └── invalid/
├── src-packages/
├── patches/
└── config.yaml
```

3. Die Plugin-Definitionsdatei erstellen (siehe [Plugin-Definitionsdatei](#plugin-definitionsdatei-configyaml)).
4. Test-Instanzen vorbereiten (siehe [Test-Instanzen](#testinstanzen))
5. (optional) Quell-FHIR-Packages ablegen (siehe [Quell-FHIR-Packages](#quell-fhir-packages))
6. (optional) Patches für die Ressourcen in den Quell-FHIR-Packages ablegen (
   siehe [Patches für die Quell-FHIR-Packages](#patches-für-die-quell-fhir-packages))

#### Plugin-Definitionsdatei (`config.yaml`)

> **Warning**
> Seit der Version 2.x des PluginBuilders wird ausschließlich die Version 2.0 der Plugin-Definitionsdatei unterstützt.
> Die Version 1.x (siehe git-History der README.md für weitere Informationen) wird nicht mehr unterstützt.
> Mit der Version 2.0 der Plugin-Definitionsdatei ist es möglich, mehrere FHIR-Packagelisten zu definieren, die von
> einem Plugin validiert werden können.

| Eigenschaft                                                               | Bezeichnung in config.yaml                                     | Pflichtangabe | Beschreibung                                                                                                                                                                                                                                                                                                                                                                                               | Beispiel                                                                                                                                                                                          |
|---------------------------------------------------------------------------|----------------------------------------------------------------|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Version der Definition-Beschreibungssprache                               | configSpecVersion                                              | Ja            | Derzeit fest auf 2.0                                                                                                                                                                                                                                                                                                                                                                                       | 1.0                                                                                                                                                                                               |
| Name                                                                      | id                                                             | Ja            | Identifikator des Plugins. Wird für die Paketierung und den Aufruf im Referenzvalidator verwendet                                                                                                                                                                                                                                                                                                          | isik3-terminplanung                                                                                                                                                                               |
| Version                                                                   | version                                                        | Ja            | Version des Plugins                                                                                                                                                                                                                                                                                                                                                                                        | 1.0.0                                                                                                                                                                                             |
| Author                                                                    | author                                                         | Ja            | Author des Plugins.                                                                                                                                                                                                                                                                                                                                                                                        | gematik                                                                                                                                                                                           |
| Beschreibung                                                              | description                                                    | Ja            | Eine kurze Beschreibung des Plugins und seines Validierungsumfangs.                                                                                                                                                                                                                                                                                                                                        | Dies ist eine Beispielbeschreibung des Plugins.                                                                                                                                                   |
| Link zur Spezifikation                                                    | specUrl                                                        | Nein          | Link zur Spezifikation des validation FHIR-Packages (validation.fhirPackage)                                                                                                                                                                                                                                                                                                                               | https://simplifier.net/packages/de.gematik.isik-terminplanung/3.0.0                                                                                                                               |
| Paketlisten zur Validierung                                               | validation.dependencyLists                                     | Ja            | Eine Paketliste besteht mindestens aus einem Hauptpackage, dessen Profile validiert werden sollen.                                                                                                                                                                                                                                                                                                         |                                                                                                                                                                                                   |
| Paketname                                                                 | validation.dependencyLists[x].fhirPackage                      | Ja            | Name und Version des FHIR-Packages, dessen Profile vom Plugin validiert werden sollen (Format abc#X.Y.Z)                                                                                                                                                                                                                                                                                                   | kbv.ita.erp#1.1.0                                                                                                                                                                                 |
| Validierungsabhängigkeiten                                                | validation.dependencyLists[x].dependencies                     | Nein          | Liste von FHIR-Packages (Format abc#X.Y.Z), die aus dem Package-Server heruntergeladen werden sollen und die später für die Validierung benötigt werden (falls abweichend von der package.json des Haupt-Packages unter `fhirPackage` und falls das Package *nicht* im `src-packages`-Ordner abgelegt wurde)                                                                                               | - "kbv.basis#1.3.1"<br/>- "kbv.ita.for#1.3.1"                                                                                                                                                     |
| Gültigkeitsdatum Von                                                      | validation.dependencyLists[x].validFrom                        | Nein          | Starddatum der Gültigkeit der Paketliste (MEZ)                                                                                                                                                                                                                                                                                                                                                             | "2023-01-01"                                                                                                                                                                                      |
| Gültigkeitsdatum Bis                                                      | validation.dependencyLists[x].validTill                        | Nein          | Enddatum der Gültigkeit der Paketliste (MEZ)                                                                                                                                                                                                                                                                                                                                                               | "2023-01-31"                                                                                                                                                                                      |
| Selektionsausdrücke für Instanzerstellungsdatum                           | validation.dependencyLists[x].creationDateLocators             | Nein          | Mit den Selektionsausdrücke für Instanzerstellungsdatum kann das Plugin prüfen, ob die zu validierende FHIR-Ressource während der Gültigkeitsperiode der entsprechenden Packagesliste erstellt wurde                                                                                                                                                                                                       |                                                                                                                                                                                                   |
| Profil für den Selektionsausdruck                                         | validation.dependencyLists[x].creationDateLocators[x].profile  | Nein          | Wenn das Profil-CanonicalUrl in einer FHIR-Ressource referenziert wird, prüft das Plugin mittels des angegebenen fhirPath-Ausdrucks, ob das Erstellungsdatum dieser Ressource in der Gültigkeitsperiode der entsprechenden Packagesliste liegt                                                                                                                                                             | "https://gematik.de/fhir/isik/v3/Terminplanung/StructureDefinition/ISiKTermin"                                                                                                                    |
| [FHIRPath](https://hl7.org/fhirpath/)-Ausdruck für den Selektionsausdruck | validation.dependencyLists[x].creationDateLocators[x].fhirPath | Nein          | Mit dem FHIRPath-Ausdruck wird ein Ausdruck angegeben oder ein Element der zu validierenden FHIR-Ressource selektiert, an dem das Erstellungsdatum dieser Ressource abgelesen werden kann                                                                                                                                                                                                                  | "Appointment.start"                                                                                                                                                                               |
| Kritikalitätsbewertung der Validierungsausgaben                           | validation.dependencyLists[x].validationMessageTransformations | Nein          | Veränderung der Kritikalitätsstufe der Ausgaben des Validators.                                                                                                                                                                                                                                                                                                                                            | transformation1:<br>- severityLevelFrom: "error"<br>- severityLevelTo: "information"<br>- locatorString: "Appointment.participant:AkteurPatient"<br>- messageId: "Validation_VAL_Profile_Minimum" |
| Zu ignorierende Codesysteme                                               | validation.ignoredCodeSystems                                  | Nein          | CodeSysteme, die im Rahmen der Validierung nicht überprüft werden sollen                                                                                                                                                                                                                                                                                                                                   | "http://fhir.de/CodeSystem/bfarm/icd-10-gm"                                                                                                                                                       |
| Zu ignorierende ValueSets                                                 | validation.ignoredValueSets                                    | Nein          | ValueSets, die im Rahmen der Validierung nicht überprüft werden sollen                                                                                                                                                                                                                                                                                                                                     | "http://fhir.de/ValueSet/bfarm/ops"                                                                                                                                                               |
| Unbekannte Extensions führen zu Fehlern                                   | validation.anyExtensionsAllowed                                | Nein          | Festlegung, ob unbekannte Extensions in den Instanzen zu Fehlern führen sollen                                                                                                                                                                                                                                                                                                                             | false                                                                                                                                                                                             |
| Unbekannte Profile führen zu Fehlern                                      | validation.errorForUnknownProfiles                             | Nein          | Festlegung, ob unbekannte Profile bei meta.profile-Angaben in den Instanzen zu Fehlern führen sollen                                                                                                                                                                                                                                                                                                       | true                                                                                                                                                                                              |
| Akzeptierte Dateiformate                                                  | validation.acceptedEncodings                                   | Nein          | Festlegung, welche Dateiformate bei Validierung ausschließlich akzeptiert werden sollen                                                                                                                                                                                                                                                                                                                    | xml                                                                                                                                                                                               |
| Snapshot-Abhängigkeiten                                                   | snapshotDependencies                                           | Nein          | Liste von zusätzlichen FHIR-Packages (Format abc#X.Y.Z), die nur für die Snapshot-Generierung benötigt werden. Wenn die Packages nicht im Abhängigkeitsbaum der Validierungspackages, dann werden sie heruntergeladen und beim Snapshot-Generierung verwendet. Wenn die Packages in dem Abhängigkeitsbaum der Validierungspackages auftauchen, dann werden sie aus dem finalen Abhängigkeitsbaum entfernt. | "ein-nur-fuer-snapshot-generierung-benoetigtes-package#x.y.z"                                                                                                                                     |

Beispiel einer `config.yaml`-Datei:

```yaml
configSpecVersion: "2.0"
id: "isik3-terminplanung"
version: "1.0.0"
author: "gematik"
description: "Dies ist eine Beispielbeschreibung des Plugins."
specUrl: "https://simplifier.net/packages/de.gematik.isik-terminplanung/3.0.0"
validation:
  dependencyLists:
    - fhirPackage: "de.gematik.isik-terminplanung#3.0.0"
      dependencies:
        - "kbv.basis#1.3.1"
        - "kbv.ita.for#1.3.1"
      validFrom: "2023-01-01"
      validTill: "2023-01-31"
      creationDateLocators:
        - profile: "https://gematik.de/fhir/isik/v3/Terminplanung/StructureDefinition/ISiKTermin"
          fhirPath: "Appointment.start"
      validationMessageTransformations:
        transformation1:
          severityLevelFrom: "error"
          severityLevelTo: "information"
          locatorString: "Appointment.participant:AkteurPatient"
          messageId: "Validation_VAL_Profile_Minimum"
  errorOnUnknownProfile: "true"
  anyExtensionsAllowed: "false"
  acceptedEncodings:
    - "xml"
  ignoredCodeSystems:
    - "http://snomed.info/sct"
  ignoredValueSets:
    - "http://fhir.de/ValueSet/bfarm/ops"
snapshotDependencies:
  - "ein-nur-fuer-snapshot-generierung-benoetigtes-package#x.y.z"
```

#### Testinstanzen

Im Ordner `test-files` müssen mindestens eine valide (im Unterordner `valid`) sowie eine invalide (im Unterordner
`invalid`) FHIR-Ressource als Testinstanz von einem Profil aus `validation.fhirPackage` hinterlegt werden, um
sicherzustellen, dass die Validierung mit dem gebauten Plugin auch funktioniert. Im besten Fall sollte für jedes
enthaltene Profil aus `validation.dependencyLists[x].fhirPackage` jeweils eine valide und eine invalide FHIR-Ressource
als Testinstanz hinterlegt werden. Der PluginBuilder weist in seiner Konsolenausgabe auf die Profile mit fehlenden
Testinstanzen hin.

#### Quell-FHIR-Packages

Im Ordner `src-packages` können FHIR-Packages
im [FHIR Package Standard](https://confluence.hl7.org/display/FHIR/NPM+Package+Specification) hinterlegt werden. Die
hier hinterlegten FHIR-Packages werden zusätzlich zu den Abhängigkeiten des Plugin-Pakets (siehe
`validation.fhirPackage`) für die Validaierung von FHIR-Ressourcen verwendet.

#### Patches für die Quell-FHIR-Packages

Hier können einzelne FHIR-Ressourcen hinterlegt werden, die die FHIR-Ressourcen in originellen FHIR-Packages ergänzen
oder ersetzen sollten (z.B. für den Fall wenn in einem Profil eines bereits veröffentlichten Quell-FHIR-Package ein
Fehler vorliegt, der die Validierung verhindert).

** Warning **
> Die Dateien müssen exakt denselben Namen haben, wie die fehlerhaften Dateien im Quell-FHIR-Package. Sie müssen im
> Unterordner abgelegt werden, dessen Name im Format [FHIR-Package-Name]-[FHIR-Package-Version] dem Quell-FHIR-Package
> entspricht

Beispiel:

```
my-plugin/
└── patches/
    └── package1-1.0.0/
        └── some-profile-with-fixes.json
```

### Aufruf des Plugin-Builders zur Erstellung von Plugins

> **Warning**
> Um Abhängigkeiten der FHIR-Pakages zu herunterladen, erfordert der Plugin-Builder direkte Verbindung zum Internet.
> Derzeit ist keine Proxy-Server-Konfiguration möglich

Beispielaufrufe des Plugin-Builders (build):

`java -jar plugin-builder-cli-X.Y.Z.jar build C:\user\plugindefinitions\my-plugin`

`java -jar plugin-builder-cli-X.Y.Z.jar build C:\user\plugindefinitions\my-plugin -t C:\output`

Das erste Argument ('build') gibt an, dass der build-Prozess gestartet werden soll. Das zweite Argument ist
verpflichtend und gibt den Pfad zum Ordner mit Plugin-Ressourcen an. Optional gibt es folgende Parameter:

| Parameter                            | Beschreibung                                                                                                                                 |
|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `-t` oder<br/>`--targetFolderPath`   | Ausgabeverzeichnis. Standardmäßig wird das Parent-Verzeichnis vom Plugin-Ressourcen-Ordner als Ausgabeverzeichnis verwendet                  |
| `-url` oder<br/>`--packageServerUrl` | URL eines alternativen FHIR Package Servers. Standardmäßig werden http://packages.fhir.org und https://packages2.fhir.org/packages verwendet |

Neben Konsolenausgabe schreibt der Plugin-Builder während der Ausführung ein Protokoll mit WARN- und ERROR-Meldungen,
die von der zugrundeliegenden HAPI-Bibliothek während der Snapshot-Generierung produziert wurden (`warn.log`).

### Testen von Plugins mittels des Plugin-Builders

Mithilfe des Plugin-Builders können auch bereits gebaute Plugins getestet werden.

Beispielaufrufe des Plugin-Builders (test):

`java -jar plugin-builder-cli-X.Y.Z.jar test C:\user\plugindefinitions\my-plugin.zip C:\user\test-files`

Das erste Argument ('test') gibt an, dass der test-Prozess gestartet werden soll. Hier sind auch die beiden folgenden
Argumente verpflichtend. Das zweite gibt den Pfad zum zu testenden Plugin an und das dritte ist der Pfad zum Ordner, der
die Testinstanzen enthält.

### Bauen von Plugins mit Hilfe von Maven

Vor dem Bauen der Plugins muss zuerst das PluginBuilder-Projekt gebaut werden. Dazu wird der Befehl
`mvn clean package -pl :plugin-builder` im Hauptverzeichnis des PluginBuilder-Projekts aufgerufen.

Alle bereits integrierten Plugins können anschließend mit dem Aufruf `mvn clean install -Pbuild-plugins` gebaut werden.
Um ein einzelnes, spezielles Plugin zu bauen wird folgender Aufruf benötigt:
`mvn clean install -pl :valmodule-eeb -Pbuild-plugins`. Dieser Aufruf würde das `valmodule-eeb` einzeln bauen.

## Beiträge zum Projekt

Für Beiträge zum PluginBuilder oder den Plugins siehe [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

Copyright 2025-2026 gematik GmbH

Apache License, Version 2.0

See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.

## Additional Notes and Disclaimer from gematik GmbH

1. Copyright notice: Each published work result is accompanied by an explicit statement of the license conditions for
   use. These are regularly typical conditions in connection with open source or free software. Programs
   described/provided/linked here are free software, unless otherwise stated.
2. Permission notice: Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
   associated documentation files (the "Software"), to deal in the Software without restriction, including without
   limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
   Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
1. The copyright notice (Item 1) and the permission notice (Item 2) shall be included in all copies or substantial
   portions of the Software.
2. The software is provided "as is" without warranty of any kind, either express or implied, including, but not limited
   to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or
   copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of
   or in connection with the software or the use or other dealings with the software, whether in an action of contract,
   tort, or otherwise.
3. The software is the result of research and development activities, therefore not necessarily quality assured and
   without the character of a liable product. For this reason, gematik does not provide any support or other user
   assistance (unless otherwise stated in individual cases and without justification of a legal obligation).
   Furthermore, there is no claim to further development and adaptation of the results to a more current state of the
   art.
3. Gematik may remove published results temporarily or permanently from the place of publication at any time without
   prior notice or justification.
4. Please note: Parts of this code may have been generated using AI-supported technology. Please take this into account,
   especially when troubleshooting, for security analyses and possible adjustments.

## Kontakt

Fragen, Anregungen, Bug Reports und Feature requests sind willkommen und können gerne über
die [GitHub Issues](https://github.com/gematik/app-referencevalidator-plugins/issues) oder
über [referenzvalidator&commat;gematik.de](mailto:referenzvalidator&commat;gematik.de) eingereicht werden.

