# Development

Requires the android sdk installed and the ANDROID_HOME environment variable set:
```bash
export ANDROID_HOME=/Users/{user}/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```
## Testing
### Intellij
Android instrumented tests can be run by running them through IntelliJ with the Android plugin or Android Studio.
The `MOMENTO_API_KEY`, `V1_API_KEY`, and `MOMENTO_ENDPOINT` environment variables must be set in your `.zshrc` or somewhere else IntelliJ can read it when it executes the gradle project. The tests cannot directly read environment variables from the host machine, so they are read by the gradle project and stored in a test property.

### Command Line
Android instrumented tests can be run from the command line by first starting an emulator:
```bash
emulator -list-avds # list available emulators
emulator -avd {one of the listed emulators} # start the emulator
```
Then run the tests:
```bash
./gradlew connectedAndroidTest
```
`MOMENTO_API_KEY`, `V1_API_KEY`, and `MOMENTO_ENDPOINT` must be set for the tests to work.
