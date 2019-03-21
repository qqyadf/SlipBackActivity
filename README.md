# SlipBackActivity

包含右滑Activity返回和右划Dialog返回功能，

SlipBackActivity使用：
1.在Application中注册activity的生命周期监听
  SlipActivityManager.getInstance().registerActivityLifecycleMonitor(this);
2.需要滑动的Activity继承自SlipBackActivty

SlipBackDialog使用：
1.创建Dialog，继承自SlipBackDialog即可。

