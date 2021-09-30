# bandwidth-checker

[![Release](https://jitpack.io/v/rajatbeck/bandwidth-checker.svg)](https://jitpack.io/#rajatbeck/bandwidth-checker)

## Integration ##

**settings.gradle**
```
allprojects {
  repositories {
		...
		maven { url 'https://jitpack.io' }
	}
 }
``` 
**build.gradle**
```
id 'kotlin-kapt'

android{
  ...
  dataBinding {
        enabled true
    }
}

dependencies {
	implementation 'com.github.rajatbeck:bandwidth-checker:$version'
}
```  
