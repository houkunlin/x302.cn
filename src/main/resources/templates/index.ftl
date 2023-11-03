<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>短链接服务</title>
</head>
<body>
<h1>短链接服务</h1>
<form action="./" method="get">
    <label for="url">链接：</label>
    <input id="url" name="url" value="${url!}">
    <button type="submit">生成短链</button>
</form>
<#if vo??>
    <p>输入URL：${url}</p>
    <p>解析URL：${vo.urlRaw}</p>
    <p>短链URL：<a href="${jumpUrl}${vo.urlKey}" target="_blank">${jumpUrl}${vo.urlKey}</a></p>
</#if>
<div />
<p style="font-weight: bold">本站服务地址：${host}</p>
<pre>
    免责声明

    本网站及其内容受中国法律的管辖。用户在使用本网站时，应遵守中国的法律和规定。

    本网站仅提供短链接跳转功能，不对用户提交的网络地址和其内容负有任何责任。

    我们不对任何由于使用本网站或依赖本网站信息而导致的损失、损害或后果承担任何责任。

    用户在使用本网站时，需自行承担所有风险和责任，包括但不限于，对任何链接、信息或数据的真实性、准确性、完整性、合法性、安全性或可用性负责。

    本网站不承担任何由于用户行为或信息使用而导致的损失、损害或后果负有任何责任。

    用户需对自己提交的网站地址负责，禁止提交非法网站，包括但不限于，涉黄、涉赌、涉毒、政治敏感、种族歧视、恐怖组织、非法组织、非法活动等中国法律明确规定不允许的网站内容。

    在您提交网址时，我们将记录您的IP地址信息，如果您的IP与创建短链接时记录的IP相同，您可删除相关短链接。
</pre>
最新短链：
<table border="1" style="width: 100%">
    <thead>
    <tr>
        <td style="width: 160px">创建时间</td>
        <td>短链地址</td>
        <td>原始地址</td>
        <td style="width: 100px">访问次数</td>
        <td>操作</td>
    </tr>
    </thead>
    <#list data as item>
        <tr>
            <td>${item.createdTime.format("yyyy-MM-dd HH:mm:ss")}</td>
            <td><a href="${jumpUrl}${item.urlKey}" target="_blank">${jumpUrl}${item.urlKey}</a></td>
            <td><a href="${item.urlRaw}" target="_blank">${item.urlRaw}</a></td>
            <td>${item.visitNum}</td>
            <td>
                <#if currentIp == item.createdIp>
                    <a href="javascript:void(0);" onclick="deleteUrl('${item.urlKey}');">删除</a>
                </#if>
            </td>
        </tr>
    </#list>
</table>
<script type="text/javascript">
function deleteUrl(key) {
  fetch('${jumpUrl}' + key + '?action=delete', { method: 'post' }).then(() => {
    window.location.href = '${host}'
  })
}
</script>
</body>
</html>
