# 侧滑删除


1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
	      maven { url 'https://jitpack.io' }
	}
}
```
2.Add the dependency
```
dependencies {
	 implementation'com.github.ALguojian:AlSwipeRemoveLayout:1.0'
}
```