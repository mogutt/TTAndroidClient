# mogujie Open Source IM App  ![Logo](https://avatars2.githubusercontent.com/u/8542441?v=2&s=200)

[mogujie](http://www.mogujie.com) Open Source IM is aiming to provide another IM solution in your company for colleagues to communicate with each other. 

we've released Win/Mac/Android/iOS  client repositories in github as well as IM server repository.

see all projects in our [mogutt](https://github.com/mogutt) github account page or visit our [ website](http://tt.mogu.io/)(In Chinese) for more information

## Android Client Features
* list all colleagues in your company as well as detail profiles like (email addr, title, phone No. etc.)
* support fast search for colleague profiles
* support communicating through "Text", "Audio", "Image" messages like [whatsapp](http://www.whatsapp.com/) 
* support creating temporary chat group with multiple people all together
* support permanent chat group created by administrators
* support basic functions such as Emotion、Vibrating screen、File transfer and so on


## Building

The build requires the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed.

We're using [eclipse](https://www.eclipse.org/home/index.php) to build the project right now. so you also have to install [ADT](http://developer.android.com/tools/sdk/eclipse-adt.html) eclipse plugin.
In future, we're going to support [Maven](http://maven.apache.org/) building system too.

Build steps:
* `git clone https://github.com/mogutt/TTAndroidClient`
* import the project -  open eclipse, and select menu `File->New->Project`, then search and select  `Android Project From Existing Project`, select the source code root directory
* 2 projects(MGTTInitAct, mgimlibs) should be imported to eclipse successfully so far
* clean the 2 projects and rebuild 
* if error is `"Unable to resolve target 'android-16'"`, edit project.properties files of the 2 projects, and modify it with your android SDK version on your machine
* if there're still a lot of errors about mgimlibs project, close the eclipse, and restart it should solve the issues
* any building issues found, please file an issue ticket in [issue page](https://github.com/mogutt/TTAndroidClient/issues)


## Manual
use below identities to login:
* **id:** eric
* **password:** 123456

* **id:** tom
* **password:** 123456


![loginpage] (http://s6.sinaimg.cn/mw690/003j8GoBgy6MMp9gLNra5&690)

![contactpage](http://s9.sinaimg.cn/mw690/003j8GoBgy6MMp9dHmM38&690)

or you can go to our official [website](http://tt.mogu.io/home/sign)(In Chinese) to register an account


## Acknowledgements

This project uses some open sources libraries such as:
* [Square/Picasso](https://github.com/square/picasso)


These are just a few of the major dependencies, we're going to add the entire list of libraries soon.

**Thanks** to those Open Source libraries, without them, this repository would never exists at all! :)

Please **inform** us if anything mis-used with Open Source libraries, we're kind of lacking experience in Open Source development right now, and be willing and happy to take adavice to make this project better and truly make it looks like the "Open Source" way. 

please go to the [issue page](https://github.com/mogutt/TTAndroidClient/issues)

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/github/android/pulls).

Any contributions, large or small, major features, bug fixes, additional
language translations, unit/integration tests are **welcomed** and **appreciated**!


***Happy Hacking and Have Fun!***
