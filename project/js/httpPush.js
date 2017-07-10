// HttpPush.js v.1.0.0.
//
(function(global) {

var _u = undefined;
var _n = null;
var o = {};

// ajax.
var ajax=function(){var e=function(){return(new Date).getTime()},t={ajax:function(){var t=void 0,r=!1,o=!1,a="Msxml2.XMLHTTP",i=[a+".6.0",a+".3.0","Microsoft.XMLHTTP"];try{new XDomainRequest,r=!0,o=!0}catch(u){try{new ActiveXObject(a),r=!0}catch(c){}}var s=function(){var e;if(r)for(var a=0;a<n.length;a++)try{new ActiveXObject(i[a]),e=function(){return new ActiveXObject(i[a])};break}catch(u){}return e==t&&(e=function(){return new XMLHttpRequest}),o?function(n){if(1==n){var t=new XDomainRequest;return t.ie=0,t}return e()}:e}(),f=function(e,n){"POST"!=e&&"DELETE"!=e||n.setRequestHeader("Content-type","application/x-www-form-urlencoded")},p=function(e,n){if(n)for(var t in n)e.setRequestHeader(t,n[t])};return function(n,r,o,a,i){n=(n+"").toUpperCase(),r=r+(-1==r.indexOf("?")?"?":"&")+e();var u="";if("string"!=typeof o)for(var c in o)u+="&"+c+"="+encodeURIComponent(o[c]);else u=o;if("GET"==n&&(r+=u,u=null),i==t){var v=s();return v.open(n,r,!1),f(n,v),p(v,a),v.send(u),v.responseText}var v=s(/^https?:\/\//i.test(r)?1:0);0==v.ie?(v.onprogress=function(){},v.onload=function(){i(v.status,v.responseText)},v.open(n,r)):(v.open(n,r,!0),v.onreadystatechange=function(){4==v.readyState&&i(v.status,v.responseText)}),f(n,v),p(v,a),v.send(u)}}()};return t.ajax}();

// httpPush受付処理を生成.
// domain 接続先のドメイン(http://sample.com)を設定します.
//
// httpPush受付処理が返却されます.
o.reception = function(domain) {
    if(domain == _u || domain == _n) {
        return null;
    }
    var ret = {};
    var uuid = "";
    
    // UUIDを取得.
    // uuidが返却される.
    ret.uuid = function() {
        return uuid;
    }
    // 接続処理.
    // call コールバックメソッド.
    //      uuidがセットされる.
    ret.connect = function(call) {
        if(uuid != "") {
            call("");
            return;
        }
        ajax("GET",domain+"/create","",null,function(state,res) {
            res = eval("["+res+"]")[0];
            uuid = res.uuid;
            call(uuid);
        });
    }
    // 切断処理.
    // call コールバックメソッド.
    //      接続状況が切断された場合は[true]がセットされる.
    ret.close = function(call) {
        if(uuid == "") {
            call(false);
            return;
        }
        ajax("GET",domain+"/clear/" + uuid,"",null,function(state,res) {
            res = eval("["+res+"]")[0];
            uuid = "";
            call(res.result);
        });
    }
    // reconnect.
    // uuid を設定して、以前の接続を再利用する.
    ret.reconnect = function(id) {
        if(id == _u || id == _n) {
            id = "";
        }
        uuid = id;
    }
    // 設定データ数取得.
    // call コールバックメソッド.
    //      現在格納されている（送信されていない）データ数が返却される.
    ret.size = function(call) {
        if(uuid == "") {
            call(-1);
            return;
        }
        ajax("GET",domain+"/size/" + uuid,"",null,function(state,res) {
            res = eval("["+res+"]")[0];
            call(res.size);
        });
    }
    // データセット.
    // data 送信対象の文字列をセット.
    // call コールバックメソッド.
    //      正しく処理された場合は[true]がセットされる.
    ret.send = function(data,call) {
        if(uuid == "") {
            call(false);
            return;
        }
        ajax("POST",domain + "/send/" + uuid,data,null,function(state,res) {
            res = eval("["+res+"]")[0];
            call(res.result);
        });
    }
    // 直接データセット.
    // uuid 対象のUUIDを設定します.
    // data 送信対象の文字列をセット.
    // call コールバックメソッド.
    //      正しく処理された場合は[true]がセットされる.
    ret.connectSend = function(uuid,data,call) {
        ret.reconnect(uuid);
        ret.send(data,call);
    }
    return ret;
}

// HttpPushServerSentEvent処理を生成.
// domain 接続先のドメイン(http://sample.com)を設定します.
// uuid 接続先のUUIDを設定します.
//      このUUIDは、HttpPush.reception.createで取得したUUIDをセットします.
//
// HttpPushServerSentEvent処理が返却されます.
o.sse = function(domain,id) {
    if(domain == _u || domain == _n || id == _u || id == _n) {
        return null;
    }
    var ret = {};
    var uuid = id;
    var sse = null;
    
    // 接続処理.
    // call コールバックメソッド.
    //      データを受信した場合は、第一引数に[true],第二引数に[event]情報がセットされる.
    //      エラーが発生した場合は、第一引数に[false],第二引数に[event]情報がセットされる.
    ret.connect = function(call) {
        if(sse != null) {
            ret.close();
        }
        sse = new EventSource(
            domain + "/" + document.domain + "/" + uuid + "/" + window.location.protocol,
            {withCredentials: true});
        sse.onmessage = function(e) {
            
            // データが無効な場合は受け取らない.
            if(e.data == _u || e.data == _n || e.data == "") {
                return;
            }
            call(true,e);
        };
        sse.onerror = function(e) {
            
            // 通信が切断している場合は、コネクションを明示的に破棄.
            if(!ret.isConnect()) {
                ret.close();
            }
            call(false,e);
        }
    }
    // 切断処理.
    ret.close = function() {
        if(sse != null) {
            try {
                sse.close();
            } catch(e) {
            }
            sse = null;
        }
    }
    // uuidを取得.
    ret.uuid = function() {
        return uuid;
    }
    // 接続状態を取得.
    ret.isConnect = function() {
        if(sse == null) {
            return false;
        }
        return (sse.readyState == 1);
    }
    return ret;
}

global.HttpPush = o;
})(window);
