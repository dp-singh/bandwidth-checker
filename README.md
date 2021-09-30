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

## How to use ## 

To launch the module you just need to attach Bandwidth fragment to your activity or fragment. 

```
supportFragmentManager
            .beginTransaction()
            .replace(
                findViewById<FrameLayout>(R.id.root_view).id,
                BandwidthFragment.newInstance(),
                BandwidthFragment::class.java.simpleName
            )
            .addToBackStack(null)
            .commit()
```	    
