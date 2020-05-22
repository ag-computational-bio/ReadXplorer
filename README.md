ReadXplorer
===========

Visualization and Analysis of Mapped Sequences
-----------

ReadXplorer is a freely available comprehensive exploration and evaluation tool for NGS data. It extracts and adds quantity and quality measures to each alignment in order to classify the mapped reads. This classification is then taken into account for the different data views and all supported automatic analysis functions.

ReadXplorer is implemented in Java as a Netbeans rich client application. Utilizing a modular programming structure, it enables developers to create their own highly specialized software modules and easily plug them into ReadXplorer.

Here, the **source code** of ReadXplorer is freely available. For **more information** and the **download for end-users** please visit http://www.readxplorer.org


-----------
When developing code for ReadXplorer, we advise you to use the following **Maven repositories** in your Netbeans ```settings.xml``` to resolve all dependencies:
```
        <repository>
          <id>netbeans</id>
          <url>http://bits.netbeans.org/maven2/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
        </repository>
        <repository>
          <id>central</id>
          <url>http://central.maven.org/maven2/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
        </repository>
        <repository>
          <id>sonatype</id>
          <url>https://oss.sonatype.org/content/repositories/releases/</url> 
          <releases>
            <enabled>true</enabled>
          </releases>
        </repository>
```
```
        <pluginRepository>
          <id>netbeans</id>
          <url>http://bits.netbeans.org/maven2/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
        </pluginRepository>
        <pluginRepository>
          <id>central</id>
          <url>http://central.maven.org/maven2/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
        </pluginRepository>
        <pluginRepository>
          <id>sonatype</id>
          <url>https://oss.sonatype.org/content/repositories/releases/</url>
          <releases>
            <enabled>true</enabled>
          </releases>
        </pluginRepository>
```
