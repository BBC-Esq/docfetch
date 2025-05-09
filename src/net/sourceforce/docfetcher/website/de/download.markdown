Schritt 1: Java-Laufzeit-Umgebung installieren
====================================
Eine installierte Java-Laufzeit-Umgebung (JRE), Version 1.7 oder neuer, ist erforderlich. Um herauszufinden, welche JRE installiert ist, können Sie eine Eingabeaufforderung (Konsole) öffnen und eingeben: "java -version"

Falls Java 1.7 oder neuer nicht auf Ihrem System verfügbar ist, können Sie es von folgenden Orten beziehen:

* Windows: <https://java.com>
* Linux: Im offiziellen Software-Repository Ihrer Distribution.
* OS&nbsp;X: <https://java.com>. Wichtig: Selbst mit Apple-M1-Chip muss die Java-Laufzeit-Umgebung für die x64-Architektur installiert werden, nicht die für die ARM64-Architektur.

Schritt 2: DocFetcher ${version} installieren
======================================

Für eine Liste von Änderungen im Vergleich zu vorherigen Versionen, siehe den [ChangeLog](http://docfetcher.sourceforge.net/wiki/doku.php?id=changelog).

Alle unten aufgeführten Downloads unterstützen sowohl 32-Bit- als auch 64-Bit-Betriebssysteme.

<table>
<tr>
<th>Download & Installations-Hinweise</th>
<th>Unterstützte Betriebssysteme</th>
</tr>
<tr>
<td align="left"><a href="https://sourceforge.net/projects/docfetcher/files/docfetcher/${version}/docfetcher_${version}_win32_setup.exe/download">docfetcher_${version}_win32_setup.exe</a> <br/> Führen Sie das Installations-Programm aus und folgen Sie den Anweisungen.</td>
<td>Windows</td>
</tr>
<tr>
<td align="left"><a href="https://sourceforge.net/projects/docfetcher/files/docfetcher/${version}/docfetcher-${version}-portable.zip/download">docfetcher-${version}-portable.zip</a> <br/> Dies ist die portable Version, die auf allen unterstützten Betriebssystemen läuft. Installation: Entpacken Sie das Archiv in einen Ordner Ihrer Wahl, und starten Sie dann DocFetcher mittels Doppelklick auf die passende Datei für Ihr Betriebssystem. Stellen Sie sicher, dass Sie über Schreibrechte für den Ordner verfügen, in den DocFetcher entpackt wurde (d.&nbsp;h., dass DocFetcher nicht in einen Ordner wie bspw. "C:\Program&nbsp;Files" entpackt werden sollte).
</td>
<td>Windows, Linux, OS&nbsp;X</td>
</tr>
<tr>
<td align="left"><a href="https://sourceforge.net/projects/docfetcher/files/docfetcher/${version}/DocFetcher-${version}.dmg/download">DocFetcher-${version}.dmg</a> <br/> Starten Sie DocFetcher mittels Doppelklick auf das Application Bundle.
</td>
<td>OS&nbsp;X</td>
</tr>
</table>

Andere Downloads
================
DocFetcher ist als Portable-Apps-Version verfügbar, siehe [hier](https://portableapps.com/node/53747).

Sowohl die normale portable DocFetcher-Version als auch die Portable-Apps-Version können so eingerichtet werden, dass sie ohne installierte Java-Laufzeit-Umgebung ausgeführt werden können. Für Details siehe das englischsprachige [DocFetcher-Wiki](http://docfetcher.sourceforge.net/wiki/doku.php?id=tips_tricks).

Für 64-bit-Linux ist DocFetcher als Snap-Paket verfügbar, siehe [hier](https://snapcraft.io/docfetcher).

Ältere Versionen sind auf der [SourceForge.net Download-Seite](https://sourceforge.net/projects/docfetcher/files/docfetcher/) erhältlich.

Wie man an Quell-Code gelangt, ist auf [dieser Wiki Seite](http://docfetcher.sourceforge.net/wiki/doku.php?id=source_code) erklärt.
