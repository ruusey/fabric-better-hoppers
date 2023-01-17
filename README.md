# HopR - BetterHoppers for Minecraft

![](hopr.gif)

## Setup
Becuase I write tons of java i have 100000 JDKs installed *SO PLEASE* change this line or remove it from `gradle.properties`
`org.gradle.java.home=D:/Programs/java/jdk-19.0` 
- This was something i personally needed to add to not interfere with other development environments I have locally :)

Requireds Fabric Loader 0.14.12 (client) And Fabric API (server) to run this mod on your server. Get them [HERE](https://fabricmc.net/develop/)

No theres no setup or additional configuration
this item is stupid OP. So ```/give @p hopr:hopperplus``` 
and enjoy your unthrottled item transfer 
*(20/s)* assuming your server runs under 50ms per tick

## Manual Building

you should be able to exectute `./gradlew clean build` in the root of the Project and add the outputted .JAR that is generated in `./build/libs` to your server
and client. If you can believe it i ACTUALLY tested it!

## License

Insert something about licensing. take from this and learn my child.
