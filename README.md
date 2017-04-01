# sso-parent
SSO单点登录

oss-cache:主要是定义一些缓存策略
sso-server:统一登录验证的地方，登录成功后会把token存入session中，并把token返回给前端，后面的服务均需要用此token验证
sso-core:主要是定义了2个过滤器及配置文件，sso-server项目需要依赖该项目，并在其中使用过滤器
sso-client-user:为了测试过滤器的效果，若没有登录的状态下直接访问，则会被重定向到oss-server的登录页面
sso-client-org:类似sso-client-user