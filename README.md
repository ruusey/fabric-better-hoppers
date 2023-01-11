# HopR - BetterHoppers for Minecraft

![](https://i.imgur.com/qhfoxte.gif)

## Setup
Becuase I write tons of java i have 100000 JDKs installed *SO PLEASE* change this line or remove it from `gradle.properties`
`org.gradle.java.home=D:/Programs/java/jdk-19.0` 
- This was something i personally needed to add to not interfere with other development environments I have locally :)

Required fabric loader 0.14.11 And Fabric API to run on your server/Fabric Loader to run on your client.
No theres no setup or config this item is stupid OP. `/give @p hopr:betterhopper` 
and enjoy your unthrottled item transfer 
*(20/s)* assuming your server isnt garbo

## Manual Building

you should be able to exectute `./gradlew clean build` in the root of the Project and add the outputted .JAR that is generated in `./build/libs` to your server
and client. If you can believe it i ACTUALLY tested it!

## License

Insert something about licensing. take from this and learn my child.
