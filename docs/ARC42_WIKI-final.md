# Lombardio Architektur-Wiki (arc42)

Dieses Dokument beschreibt die Softwarearchitektur der Cloud-Plattform **Lombardio** für den Handel und die Beleihung von Sachwerten.

---

## 1. Einführung und Ziele

### 1.1 Aufgabenstellung
Lombardio ist eine modulare **Open-Source-Plattform** (geplant) für Pfandleihhäuser, Juweliere und den gehobenen Gebrauchtwarenhandel. Ziel ist die rechtssichere, hochautomatisierte Abwicklung von Warenankäufen, Pfandkrediten, Bestandsverwaltungen und Auktionen für den gesamten EU-Raum.

### 1.2 Qualitätsziele
1.  **Mandantentrennung (Tenant Isolation):** Absolute Trennung der Daten zwischen verschiedenen Unternehmen.
2.  **Revisionssicherheit (Auditability):** Lückenlose, manipulationssichere Protokollierung aller geschäfts- und compliance-relevanten Aktionen.
3.  **Sicherheit:** Backend-erzwungene Autorisierung und Identitätsprüfung (IAM) auf Banken-Niveau.
4.  **Automatisierung:** Maximale Entlastung des Personals durch intelligente Workflows (KI-Bewertung), Hardware-Anbindung (Waagen) und digitale Signaturen.
5.  **Multilateralität & Compliance:** Dynamische Abbildung verschiedener Rechtshoheiten (Jurisdictions) und Branchenregeln (PfandlV, GewO, CCD II).
6.  **Einfachheit (KISS):** Modulare Architektur, die es ermöglicht, nur die benötigten Funktionen (z.B. nur Goldankauf) zu nutzen.

### 1.3 Stakeholder
| Rolle | Erwartung |
| :--- | :--- |
| **Händler / Pfandleiher** | Schnelle Erfassung, rechtssichere Verträge, automatisierte Bestandsführung. |
| **Juweliere / An- & Verkauf** | Korrekte Abwicklung der Differenzbesteuerung, einfache Goldwertermittlung. |
| **Plattform-Betreiber** | Einfache Mandantenverwaltung, Skalierbarkeit, EU-weite Einsetzbarkeit. |
| **Auditoren/Behörden** | Einhaltung von GwG, KassenSichV und PfandlV; nachvollziehbare Historie. |

---

## 2. Randbedingungen

### 2.1 Technische Randbedingungen
*   **Backend:** Spring Boot (Java 21) für geschäftskritische Logik; Go für Infrastruktur/Durchsatz.
*   **Frontend:** Vue.js 3 (Vite, Pinia, PrimeVue).
*   **Infrastruktur:** PostgreSQL, Redis, RabbitMQ, Traefik (API Gateway), Keycloak (IAM).
*   **Deployment:** Docker Compose (Lokal), Kubernetes/Kustomize, Terraform.

### 2.2 Organisatorische Randbedingungen
*   **Entwicklungsmodell:** TDD (Test-Driven Development) ist obligatorisch.
*   **Architekturstil:** Hexagonale Architektur (Clean Architecture).

---

## 3. Kontextabgrenzung
### 3.1 Fachlicher Kontext
Lombardio interagiert mit:
*   **Kunden:** Über ein optionales Customer-Portal.
*   **Verkaufskanälen:** Shopware, eBay, Shopify (geplant).
*   **Bezahlsystemen:** PayPal, Wero, Banküberweisungen.
*   **Finanzwesen:** DATEV-Schnittstelle für Steuerberater und Meldungen an das Finanzamt.
*   **Compliance:** Cloud-TSE (Technische Sicherheitseinrichtung) für KassenSichV-Konformität.

---

### 3.2 Technischer Kontext
Das System läuft als Microservice-Landschaft in Kubernetes. Die Kommunikation erfolgt synchron via REST/OpenAPI und asynchron via Domain-Events (RabbitMQ). Externe Anbindungen (z.B. DATEV-Export) werden über dedizierte Integrations-Adapter realisiert.

---

## 4. Lösungsstrategie
1.  **Microservices:** Aufteilung in Bounded Contexts (Platform, Identity, Pawn-Ticket, Buy-In, etc.).
2.  **Modulare Domänen:** Klare Trennung zwischen beleihungsbasierten (Pawn) und rein handelsbasierten (Buy-In) Prozessen.
3.  **Domain-Driven Design (DDD):** Fokus auf die Gemeinsamkeiten der Sachwert-Verwaltung (Bewertung, Identität, Bestand).
4.  **Events:** Nutzung des Outbox-Patterns für konsistente Datenhaltung und Audit-Logs.
5.  **Multi-Jurisdiction Policy Engine:** Zentralisierte Verwaltung länderspezifischer Compliance-Regeln.

---

## 5. Bausteinsicht

### 5.1 Gesamtsystem (Level 1)
*   **`services/platform`:** Verwaltung von Mandanten und Features.
*   **`services/identity-intelligence`:** KYC, AML und Identitätsprüfung.
*   **`services/pawn-ticket`:** Kernlogik für Pfandkredite und Berechnungen.
*   **`services/loan-origination`:** Erstellung von Darlehensfällen.
*   **`services/integration`:** Go-basierter Event-Consumer für externe Systeme.
*   **`services/reporting`:** Zentrale Erfassung von Finanzdaten, Dashboard-Metriken und DATEV-Exporten.
*   **`services/buy-in`:** (Geplant) Eigenständiger Service für den allgemeinen Warenankauf (Juweliere, Gebrauchtwarenhandel) inklusive Differenzbesteuerung (§ 25a UStG).
*   **`frontend/app`:** Zentrales Back-Office Interface.

---

## 6. Laufzeitsicht

In diesem Abschnitt werden die zentralen Geschäftsprozesse unter Verwendung der fachlichen Domänenbegriffe detailliert beschrieben.

### 6.1 Der Pfandkredit-Lebenszyklus (Pawn Loan Lifecycle)
Dieser Prozess beschreibt den Weg von der ersten Bewertung bis zur Auflösung des Vertrags.

1.  **Darlehenserstellung (Loan Origination):**
    *   Ein **Kunde** stellt ein **Pfandgut (Collateral)** vor.
    *   Das Personal führt eine **Identitätsprüfung (KYC/AML Check)** durch.
    *   Das Pfandgut wird bewertet und ein Darlehensbetrag festgelegt.
    *   Mit der Auszahlung wird ein rechtlich bindender **Pfandschein (Pawn Ticket)** erstellt. Dieser ist der "Anker" für alle weiteren Berechnungen.
2.  **Vertragsverwaltung (Contract Management):**
    *   Während der Laufzeit kann der Kunde eine **Verlängerung (Extension)** durchführen, indem er die angefallenen Zinsen und Gebühren begleicht.
    *   Eine **Abschlagszahlung (Partial Payment)** reduziert die Darlehenssumme und führt zur Erstellung eines Folge-Pfandscheins.
3.  **Vertragsabschluss (Resolution):**
    *   **Einlösung (Redemption):** Der Kunde zahlt Darlehen, Zinsen und Gebühren zurück und erhält sein Pfandgut zurück.
    *   **Verfall (Expiration):** Wird der Pfandschein nicht innerhalb der Karenzzeit eingelöst oder verlängert, gilt das Pfandgut als **verfallenes Pfand (Expired Collateral)**.

### 6.2 Verwertungszyklus abgelaufener Pfänder (Disposition Lifecycle)
Wenn ein Pfand nicht eingelöst wird, geht es in den Verwertungsprozess über.

1.  **Verwertungsvorbereitung (Disposition):**
    *   Verfallene Pfänder werden in einen **Verwertungsfall (Disposition Case)** überführt.
    *   Hier erfolgt die Entscheidung: Geht das Objekt in eine **Auktion** oder wird es zum **Freihandverkauf** freigegeben?
    *   Nach der Freigabe wird aus dem Pfandgut ein **Verkaufsartikel (Sellable Item)**.
2.  **Abwicklung:**
    *   Der Verkauf erfolgt über die integrierten Kanäle.
    *   Das System berechnet automatisch den **Verwertungsüberschuss** (Erlös abzüglich aller Forderungen) für die spätere Auszahlung an den Kunden oder Abführung an den Fiskus.

### 6.3 Handelszyklus: Direkter Ankauf & Verkauf (Trade Lifecycle)
Dieser Prozess beschreibt den Warenfluss ohne Beleihung (z.B. Goldankauf oder Juwelier-Handel).

1.  **Warenankauf (Buy-In):**
    *   Ein **Kunde** bietet einen Sachwert zum direkten Verkauf an.
    *   **Prüfung:** Identitätsprüfung (GwG) und Materialbewertung (KI/Waage).
    *   **Abschluss:** Erstellung eines Ankaufscheins und sofortiger Übergang ins Eigentum des Mandanten.
2.  **Wiederverkauf (Resale):**
    *   Der Artikel wird als **Verkaufsartikel** im Multi-Channel-Vertrieb gelistet.
    *   Bei Verkauf wird eine Rechnung unter Anwendung der **Differenzbesteuerung (§ 25a UStG)** erstellt.

### 6.4 Auktionsvorbereitung & Versteigerungs-Reporting
Die Vorbereitung einer Versteigerung unterliegt strengen gesetzlichen Auflagen (PfandlV) und erfordert eine effiziente Datenbereitstellung für Auktionatoren.

1.  **Rechtssichere Bekanntmachung (§ 9 Abs. 4 PfandlV):**
    *   Das System generiert automatisch den Text für die öffentliche Bekanntmachung.
    *   Dieser enthält: Ort/Zeit, Name des Pfandleihers, allgemeine Bezeichnung der Pfänder (z.B. "Goldschmuck") sowie die lückenlose Liste der Pfandnummern oder Nummernserien.
2.  **Auktionskatalog (Auktionatoren-Export):**
    *   Für interne oder externe Auktionatoren wird ein detaillierter **Auktionskatalog** (PDF/Excel) bereitgestellt.
    *   Inhalt: Losnummer, Gegenstandsbeschreibung, Schätzwert, Mindestgebot (limit) und ggf. interne Hinweise zum Erhaltungszustand.
3.  **Verwertungsnachweis:**
    *   Nach der Auktion wird ein Protokoll über die Zuschläge erstellt, das als Basis für die Berechnung des Verwertungsüberschusses dient.

---

## 7. Spezifikationen nach Pfandkategorien

Unterschiedliche Kategorien von Pfandgegenständen erfordern spezifische Workflows zur Lagerung, Bewertung und Informationsaufnahme, um gesetzliche Anforderungen (§ 6, 7, 8, 10 PfandlV) und Versicherungsschutz zu gewährleisten.

### 7.1 Schmuck & Uhren (Jewelry & Watches)
*   **Gesetzliche Pflichtangaben:** Gewicht und Feingehaltsstempel (z.B. "585er Gold").
*   **Informationsaufnahme:** Feingehalt, Brutto/Netto-Gewicht, Steinbesatz (Karat, Schliff), Werkgängigkeit (Uhren).
*   **Wägung:** Zwingende Nutzung einer **geeichten Präzisionswaage der Eichklasse II** (Genauigkeit min. 0,01 g / 0,001 g). Die Eichung muss alle 2 Jahre erneuert werden.
*   **Bewertung:** Tagesaktueller Börsenwert (Goldpreis-API-Integration) vs. Wiederverkaufswert.
*   **Lagerung:** VdS-zertifizierte Tresore.

### 7.2 Elektronik (Electronics)
*   **Gesetzliche Pflichtangaben:** Fabrikmarke, Modell, Seriennummer oder IMEI.
*   **Informationsaufnahme:** Optischer Zustand, Funktionstest (Akku, Display), Zubehör, Bestätigung über Factory Reset (Datenschutz).
*   **Bewertung:** Zeitwert unter Berücksichtigung der hohen Depreziationsrate (Wertverfall).
*   **Lagerung:** Trockene, temperierte Lagerung; Schutz vor statischer Aufladung.

### 7.3 Kraftfahrzeuge (Vehicles)
*   **Gesetzliche Pflichtangaben:** Hersteller, Typ, Kennzeichen, VIN (Fahrgestellnr.), Motornummer, Ersatzreifen, Nutzlast.
*   **Informationsaufnahme:** Kilometerstand, Erstzulassung, Unfallschäden, Vorhandensein von Dokumenten (Zulassungsbescheinigung I/II).
*   **Bewertung:** Marktwertermittlung (DAT/Schwacke) abzüglich Standkosten.
*   **Lagerung (§ 10 PfandlV):** Sicherer Standplatz, Batteriepflege, regelmäßiges Bewegen (Vermeidung von Standschäden).

---

## 8. Compliance, Behörden & Problemkunden

Der Umgang mit Hehlerware und problematischen Kunden ist rechtlich durch das BGB (§ 935), die PfandlV und das Geldwäschegesetz (GwG) streng reglementiert.

### 8.1 Umgang mit Hehlerware (Stolen Goods)
*   **Kein gutgläubiger Erwerb:** An abhandengekommenen Sachen kann kein Pfandrecht erworben werden (§ 935 BGB). Identifizierte Hehlerware muss entschädigungslos an den Eigentümer oder die Polizei herausgegeben werden.
*   **Prävention:** Abgleich von Seriennummern (Elektronik, Fahrräder, Werkzeuge) mit polizeilichen Fahndungslisten bei der Aufnahme.
*   **System-Vermerk:** Pfandgegenstände können im System als "POLICE_INQUIRY" oder "STOLEN_CONFIRMED" markiert werden, was die weitere Verwertung (Verlängerung/Auktion) sofort sperrt.

### 8.2 Problemkunden & Blacklisting
*   **Interne Sperrliste:** Kunden, die bereits durch Hehlerei, Betrug oder aggressives Verhalten aufgefallen sind, werden im `identity-intelligence` Service mit einem `BLOCK`-Status versehen.
*   **Warnsignale:** Fehlendes Zubehör (Ladekabel bei Handys), fehlende Eigentumsnachweise bei hochwertigen Gütern oder widersprüchliche Angaben zur Herkunft.
*   **Geldwäscheprävention (AML):** Erstellung von Verdachtsmeldungen bei ungewöhnlichen Transaktionsmustern über die goAML-Schnittstelle der FIU.

### 8.3 Zusammenarbeit mit Behörden
*   **Auskunftspflicht:** Gemäß PfandlV müssen die Geschäftsbücher (in Lombardio die digitalen Audit-Logs und Pfandregister) den Behörden auf Verlangen zur Einsicht vorgelegt werden.
*   **Sicherstellung:** Das System dokumentiert behördliche Beschlagnahmungen lückenlos, um zivilrechtliche Rückforderungsansprüche gegen den Verpfänder vorzubereiten.
*   **Tipping-Off Verbot:** Bei laufenden Geldwäsche-Verdachtsmeldungen darf der Kunde gemäß GwG nicht über die Meldung informiert werden. Das System muss entsprechende Statusanzeigen für den Kunden (Portal) unterdrücken.

---

## 9. Intelligente Rechtsautomatisierung (Legal Automation)

Lombardio nutzt Software-Logik, um die Einhaltung komplexer Fristen und Gebührenstrukturen der Pfandleiherverordnung (PfandlV) sicherzustellen.

### 9.1 Automatisierte Gebührenkontrolle (§ 10 PfandlV)
*   **Zinsdeckelung:** Das System erzwingt die gesetzliche Obergrenze von **1 % pro Monat**.
*   **Gebührenstaffelung:** Die Kostenvergütung wird automatisch basierend auf der aktuellen PfandlV-Anlage berechnet (z. B. gestaffelte Gebühren bis 300 €, freie Vereinbarkeit darüber mit Angemessenheitsprüfung).
*   **Rundungslogik:** Korrekte Berechnung von angebrochenen Monaten gemäß den kaufmännischen Gepflogenheiten der Branche.

### 9.2 Fristen- & Verwertungswächter (§ 9 PfandlV)
*   **Auktionsreife (Grace Period):** Automatische Markierung von Pfandscheinen, die frühestens einen Monat nach Fälligkeit versteigert werden dürfen.
*   **Verwertungszwang:** Überwachung der 6-Monats-Frist zur Versteigerung nach Eintritt der Verwertungsberechtigung, um Haftungsrisiken für den Mandanten zu minimieren.
*   **Karenzzeiten:** Systemseitige Sperre von Einlösungen/Verlängerungen am Tag der Auktion ("Zuschlagsschutz").

### 9.3 Automatisierte Überschussverwaltung (§ 11 PfandlV)
*   **Überschuss-Tracking:** Automatische Berechnung des Mehrerlöses nach Abzug von Darlehen, Zinsen und Kosten.
*   **Abführungs-Automatik:** Überwachung der 2-Jahres-Frist (nach dem Jahr der Verwertung). Wenn der Überschuss nicht abgeholt wird, generiert das System einen Export für die **Abführung an das zuständige Finanzamt** (Fiskus).

### 9.4 Revisionssicherheit & Aufbewahrung (§ 3 PfandlV)
*   **4-Jahres-Frist:** Sicherstellung, dass alle Buchungsbelege und Pfandregister mindestens 4 Jahre (Spezialfrist PfandlV) bzw. 10 Jahre (GoBD) im System erhalten bleiben.
*   **Unveränderbarkeit:** Nutzung des Outbox-Patterns und eines Event-Logs, um die Unveränderbarkeit der ursprünglichen Buchungen nachzuweisen.

---

## 10. Zukünftige Automatisierungspotenziale & Erweiterte Compliance

Um den Pfandleiher operativ weiter zu entlasten und die Rechtssicherheit zu erhöhen, sind folgende Erweiterungen vorgesehen:

### 10.1 Versorgungs- & Versicherungsmeldewesen (§ 8 PfandlV)
*   **Bestandsmeldung:** Automatisierte monatliche Erstellung der Summenmeldung für die Versicherung. Das System aggregiert die aktuelle Beleihungssumme und prüft, ob die Versicherungssumme (mind. 200 % des Darlehens) noch ausreichend ist.
*   **Risiko-Alerting:** Warnung bei Überschreiten von Tresor-Limits oder Versicherungsklassen pro Warengruppe.

### 10.2 Digitales Mahnwesen & Kundenbindung
*   **Ablauf-Erinnerung:** Automatisierter Versand von SMS oder E-Mails 14 Tage vor Ablauf der Verwertungsfrist. Dies reduziert die Verfallsquote und erhöht die Kundenzufriedenheit.
*   **Digitaler Pfandschein (Hybrid):** Während der Papierschein (Inhaberpapier) physisch ausgehändigt wird (§ 6 PfandlV), stellt das System parallel eine digitale Version im Kundenportal bereit, inkl. Push-Benachrichtigungen bei Statusänderungen.

### 10.3 Mobile Inventur & Lageroptimierung
*   **Barcode-Audit:** Unterstützung mobiler Endgeräte zur schnellen Durchführung der jährlichen oder stichprobenartigen Inventur durch Scannen der Pfandetiketten.
*   **Lagerplatz-Zuweisung:** Intelligente Vorschläge für Lagerorte basierend auf Kategorie (Tresor vs. Regal vs. Kfz-Stellplatz) und Wert.

### 10.4 Automatisierte Wertermittlung (Valuation Assistant)
*   **Echtzeit-Kurse:** Integration von APIs für Edelmetallpreise (Gold, Silber, Platin) zur automatischen Berechnung des Materialwerts.
*   **Marktwert-Crawler:** (Geplant) Anbindung an Marktplatz-Daten (z. B. eBay-verkauft-Listen) für eine präzisere Zeitwertermittlung bei Elektronik und Uhren.

### 10.5 DSGVO-Konforme Datenlöschung
*   **Retention Management:** Automatisierte Anonymisierung von Kundendaten nach Ablauf aller gesetzlichen Aufbewahrungsfristen (PfandlV: 4 Jahre / GoBD: 10 Jahre), um der DSGVO ohne manuellen Aufwand gerecht zu werden.

---

## 11. Dynamische Policy- & Compliance-Engine (Multi-Jurisdiction)

Um die Expansion in verschiedene europäische Märkte zu ermöglichen und die neue EU-Verbraucherkreditrichtlinie (CCD II) abzubilden, implementiert Lombardio eine dynamische Policy-Engine.

### 11.1 Konzept der Rechtshoheit (Jurisdiction)
Jeder Mandant wird einer spezifischen **Jurisdiction** (z.B. `DE`, `AT`, `FR`) zugeordnet. Diese Zuordnung steuert, welche gesetzlichen Regelwerke für die Kreditberechnung, Fristen und Compliance-Prüfungen herangezogen werden.

### 11.2 Policy-as-Code & Rule Engine
Statt länderspezifische Logik im Code zu hardcoden, werden Regeln externalisiert:
*   **Hierarchische Auflösung:** Globale Standards können durch Länder-Regeln und spezifische Mandanten-Anforderungen überschrieben werden.
*   **Dynamische Parameter:** Zinsdeckel, Gebührenstaffeln, Kündigungsfristen und AML-Schwellenwerte werden als konfigurierbare Policies geladen.
*   **Vorteil:** Schnelle Anpassung an Gesetzesänderungen (z.B. neue PfandlV-Sätze) ohne Software-Release.

### 11.3 Europaweite Compliance-Schnittstelle
*   **CCD II & eIDAS 2.0:** Unterstützung für die **EU Digital Identity Wallet**. Verträge können mittels **Qualifizierter Elektronischer Signatur (QES)** rein digital und rechtssicher (gemäß eIDAS) abgeschlossen werden, was den papierlosen Pfandschein ermöglicht.
*   **Differenzbesteuerung (Art. 311 MwSt-SystRL):** Dynamische Anpassung der Steuerberechnung (z. B. "Global Accounting" in NL vs. Einzel-Differenz in DE) und automatisierte Rechnungsstellung in der jeweiligen Landessprache.
*   **Grenzwerte:** Dynamische Steuerung von Bargeldobergrenzen (z.B. 10.000 € EU-weit vs. 1.000 € in Frankreich/Spanien).
*   **Widerrufsrecht:** Abbildung länderspezifischer Widerrufsfristen im Kreditvertrag.

### 11.4 Hardware-Anbindung & Peripherie (IoT / Edge)
Zur Automatisierung der Filialprozesse unterstützt Lombardio die direkte Anbindung von Peripheriegeräten:
*   **Waagen-Integration:** Direkte Übernahme von Gewichtsdaten aus geeichten Präzisionswaagen (Klasse II) via **Web Serial API**. Dies verhindert manuelle Tippfehler bei der Goldwertermittlung.
*   **Barcode-Scanner:** Schnelle Erfassung von Pfandscheinen und Etiketten via USB-HID oder Serial.
*   **Dokumenten-Scanner:** Automatisierte Erfassung von Ausweisdokumenten (KYC) mit optionaler OCR-Verarbeitung.
*   **Drucker:** Unterstützung für Bondrucker (Quittungen), Etikettendrucker (Pfandetiketten) und A4-Drucker (Pfandscheine/Kataloge).

### 11.5 Cloud-TSE & KassenSichV (Compliance)
Jeder Bargeldvorgang in der Filiale (Auszahlung, Einlösung, Verkauf, Ankauf) muss in Deutschland manipulationssicher signiert werden.
*   **Anbindung:** Lombardio integriert eine Cloud-TSE (z. B. Fiskaly), um Transaktionen in Echtzeit zu signieren.
*   **Belegpflicht:** Automatische Generierung von QR-Codes auf allen thermischen Quittungen und PDF-Belegen.
*   **Prüfexport:** Bereitstellung des DSFinV-K Exports für unangekündigte Kassenprüfungen.

### 11.6 Eigenständiger Ankauf-Service (Buy-In Service)
Um die Plattform für Juweliere und den allgemeinen An- und Verkauf (ohne Pfandkontext) zu öffnen, wird ein modularer Service implementiert:
*   **Rechtssichere Dokumentation (§ 38 GewO):** Erfassung von Ausweisdaten, detaillierten Warenbeschreibungen und Herkunftsnachweisen für überwachungsbedürftige Güter.
*   **Differenzbesteuerung (§ 25a UStG):** Automatische Berechnung der Umsatzsteuer auf die Marge. Erstellung von Einkaufsbelegen und Verkaufsrechnungen ohne Steuerausweis, aber mit gesetzlichem Hinweistext.
*   **Modularität:** Der Service kann unabhängig vom `pawn-ticket` Service genutzt werden, teilt sich aber die `identity-intelligence` (KYC/AML) und die `hardware-anbindung` (Waagen).

### 11.7 KI-gestützte Wertermittlung & Bilderkennung (Computer Vision)
Zur weiteren Prozessoptimierung integriert Lombardio KI-Modelle:
*   **Modell-Identifikation:** Automatisierte Erkennung von Luxusuhren und Schmuckstücken mittels Foto zur Ermittlung von Referenzpreisen (z.B. via Chrono24 API).
*   **Zustandsanalyse:** Objektive Bewertung des Erhaltungszustands durch Bildanalyse (Erkennung von Mikrokratzern oder Abnutzung).
*   **Hehlerware-Prävention:** Echtzeit-Abgleich von Bildern und Seriennummern mit internationalen Datenbanken für gestohlene Güter (z. B. Art Loss Register).

### 11.8 ESG & Kreislaufwirtschaft (Circular Economy)
Lombardio macht den ökologischen Impact der Pfandleihe messbar:
*   **CO2-Tracking:** Automatisierte Berechnung der eingesparten Ressourcen durch die Verlängerung des Produktlebenszyklus (Wiederverwendung statt Neuproduktion).
*   **Nachhaltigkeits-Reporting:** Bereitstellung von Kennzahlen für die nicht-finanzielle Berichterstattung der Mandanten.

### 11.9 Digitale Resilienz (DORA - Digital Operational Resilience Act)
Als Finanz-IKT-Dienstleister erfüllt Lombardio die strengen EU-Anforderungen an die Betriebsstabilität:
*   **ICT Risk Management:** Integrierte Frameworks zur Überwachung und Meldung von IT-Vorfällen.
*   **Resilienz-Tests:** Unterstützung von "Red Teaming" und regelmäßigen Sicherheits-Audits zur Sicherstellung der Systemverfügbarkeit.

### 11.10 Optionales Open Banking & Bonitätsprüfung (PSD2 / CCD2)
Obwohl Pfandkredite aufgrund der reinen Sachhaftung weitgehend von der strengen Bonitätsprüfungspflicht der CCD2 ausgenommen sind, bietet Lombardio ein optionales Modul für Spezialfälle:
*   **Anwendungsbereich:** Hochwert-Transaktionen (z. B. Luxusuhren, Oldtimer), bei denen eine wirtschaftliche Plausibilitätsprüfung im Rahmen der Geldwäscheprävention (AML) oder durch Versicherungen gefordert wird.
*   **Kontoinformationsdienste (AIS):** Lombardio kann als Account Information Service Provider (AISP) via PSD2-Schnittstelle (Open Banking) mit Zustimmung des Kunden dessen Bankdaten auslesen.
*   **Vorbereitung auf Zusatzgeschäfte:** Ermöglicht Mandanten (z. B. Juwelieren) künftig rechtssicher Ratenkäufe oder unbesicherte Kredite anzubieten, bei denen die CCD2-Prüfpflicht voll greift.


### 11.11 Barrierefreiheit (European Accessibility Act - EAA)
Ab Juni 2025 müssen E-Commerce- und Banking-Portale in der EU barrierefrei sein (in DE geregelt durch das BFSG).
*   **WCAG 2.1 Level AA:** Das optionale Customer-Portal von Lombardio wird streng nach diesen Richtlinien entwickelt (Tastaturbedienbarkeit, Screenreader-Kompatibilität, Farbkontraste).
*   **Compliance-Zertifizierung:** Automatisiertes Testing in der CI/CD-Pipeline zur Sicherstellung und Dokumentation der Barrierefreiheit für alle Mandanten-Instanzen.

### 11.12 Preisangaben & Verbraucherschutz (Omnibus-Richtlinie / PAngV)
Für die angeschlossenen Online-Auktionen und den Verkauf über externe Kanäle:
*   **Strengere Rabatt-Regeln:** Bei der Anzeige von Preisermäßigungen muss automatisch der niedrigste Preis der letzten 30 Tage als Referenz ausgewiesen werden.
*   **Automatisches Tracking:** Das System trackt historische Verkaufspreise, um Verstöße gegen die Preisangabenverordnung bei Marketing-Aktionen zu verhindern.

---

## 12. Glossar (Ubiquitous Language)

Um eine konsistente Kommunikation zwischen Fachabteilung und Entwicklung zu gewährleisten, werden folgende Begriffe verbindlich verwendet:

| Begriff (DE/EN) | Definition |
| :--- | :--- |
| **Mandant (Tenant)** | Die oberste organisatorische Einheit. Ein rechtlich eigenständiges Unternehmen (Pfandleiher, Juwelier, Händler). |
| **Sachwert (Asset)** | Der physische Gegenstand (Schmuck, Uhr, Kfz), der entweder beliehen oder angekauft wird. |
| **Jurisdiction** | Die rechtliche Hoheit (Land/Region), der ein Mandant unterliegt und die das Regelwerk bestimmt. |
| **Policy Engine** | Die Komponente zur dynamischen Auswertung von Geschäftsregeln und gesetzlichen Vorgaben. |
| **Pfandgut (Collateral)** | Ein Sachwert, der als Sicherheit für ein Darlehen hinterlegt wird. |
| **Pfandschein (Pawn Ticket)** | Das zentrale Vertragsdokument, das Darlehenssumme, Zinsen, Gebühren und Fristen definiert. |
| **Verfallenes Pfand (Expired Collateral)** | Pfandgut, dessen vertragliche Frist inkl. gesetzlicher Karenzzeit ohne Einlösung oder Verlängerung abgelaufen ist. |
| **Verwertungsfall (Disposition Case)** | Der Workflow, in dem entschieden wird, wie ein verfallenes Pfand rechtssicher verwertet wird (Auktion vs. Verkauf). |
| **Verkaufsartikel (Sellable Item)** | Ein Objekt, das den Verwertungsprozess durchlaufen hat oder direkt angekauft wurde und nun zum Verkauf steht. |
| **Marktplatz-Listing (Channel Listing)** | Die konkrete Ausprägung eines Verkaufsartikels auf einer externen Plattform (z.B. ein eBay-Angebot). |
| **Einlösung (Redemption)** | Die Rückzahlung des Kredits durch den Kunden gegen Rückgabe des Pfandguts. |
| **Verlängerung (Extension)** | Die Fortsetzung des Kreditvertrags durch Zahlung der bisher angefallenen Kosten. |
| **Abschlagszahlung (Partial Payment)** | Eine Teilrückzahlung des Darlehensbetrags, die die Zinslast für die Zukunft senkt. |
| **Quittung/Beleg (Receipt)** | Der Nachweis über eine finanzielle Transaktion (Verkauf, Einlösung, Verlängerung). |
| **Ankaufschein (Buy-In Receipt)** | Das rechtssichere Dokument über den direkten Ankauf eines Sachwerts durch den Händler. |
| **QES (Qualifizierte Signatur)** | Elektronische Signatur, die der handschriftlichen Unterschrift rechtlich gleichgestellt ist (eIDAS). |
| **DORA** | EU-Verordnung über die digitale operationale Resilienz im Finanzsektor. |
| **Differenzbesteuerung** | Sonderregelung (§ 25a UStG / Art. 311 EU-RL), bei der nur die Marge besteuert wird. |
| **DATEV-Export** | Export von Buchungsdaten und Stammdaten für das Steuerbüro. |
| **Verwertungsüberschuss (Surplus)** | Der Betrag, der bei einer Verwertung nach Abzug von Kredit, Zinsen und Kosten übrig bleibt und ggf. abgeführt werden muss. |
| **Auktionskatalog (Auction Catalog)** | Die strukturierte Liste aller Lose einer Auktion inklusive Beschreibungen und Limitpreisen. |
| **Bekanntmachungstext (Auction Notice)** | Der gesetzlich vorgeschriebene Text zur öffentlichen Ankündigung einer Versteigerung. |

---

## 13. Plattform-Administration & Mehrmandantenfähigkeit

Lombardio ist als Multi-Tenant-Plattform konzipiert. Die Verwaltung der Mandanten erfolgt über eine dedizierte Plattform-Administrations-Sicht, die dem Betreiber volle Kontrolle über das Ökosystem gibt.

### 13.1 Mandanten-Provisionierung (Tenant Provisioning)
Der Plattform-Administrator ist verantwortlich für das Onboarding neuer Unternehmen:
*   **Tenant-Lifecycle:** Erstellung, Sperrung (Deaktivierung) und Archivierung von Mandanten.
*   **Key-Verwaltung:** Vergabe eindeutiger technischer Identifier, die für die Tenant-Isolation auf Datenbank-Ebene genutzt werden.
*   **Jurisdiction-Zuordnung:** Festlegung des rechtlichen Rahmens (z. B. DE, AT, FR), unter dem der Mandant operiert.

### 13.2 Feature-Management & Lizenzierung
Lombardio folgt einem modularen Ansatz. Der Plattform-Admin steuert den Funktionsumfang pro Mandant:
*   **Modul-Aktivierung:** Gezieltes Freischalten von Features wie `aml-compliance`, `online-auctions` oder `buy-in-service`.
*   **Kontingente:** (Geplant) Verwaltung von Limits (z. B. Anzahl der Mitarbeiter oder maximales Pfand-Volumen).
*   **Infrastructure-as-a-Service:** Automatische Bereitstellung der notwendigen Ressourcen (z. B. Keycloak-Gruppen) bei der Erstellung eines Mandanten.

### 13.3 Globales Monitoring & Support
Für den operativen Betrieb der Plattform stehen zentrale Werkzeuge zur Verfügung:
*   **Mandanten-Dashboards:** Übersicht über die Aktivität und den Status aller angebundenen Unternehmen.
*   **Audit-Log-Einsicht:** Revisionssichere Einsicht in Systemereignisse zur Fehleranalyse und Einhaltung von Service Level Agreements (SLAs).
*   **Support-Zugang:** (Optional) Möglichkeit für Plattform-Admins, Mandanten bei technischen Problemen durch Impersonation oder spezifische Support-Rollen zu unterstützen (unter Wahrung strenger Datenschutzvorgaben).
