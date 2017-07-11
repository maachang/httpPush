// HttpPush.js v.1.0.0.
//
(function(global) {

var _u = undefined;
var _n = null;
var o = {};

// ajax.
// method POST or GET
// url 接続先のURLを設定します.
// params GET or POSTのパラメータをセットします(String or {}).
// header 送信対象のHTTPヘッダを設定します({}).
// call コールバックメソッドを設定します.
//      第一引数: ステータス, 第二引数: レスポンス(string).
var ajax = function () {
    
    // クロスドメイン対応Ajax.
    var ajax = (function(){
        var ie = false ;
        var xdom = false ;
        var ia = 'Msxml2.XMLHTTP' ;
        var iex = [ia+'.6.0',ia+'.3.0','Microsoft.XMLHTTP'] ;
        try {
            new XDomainRequest() ;
            ie = true ;
            xdom = true ;
        } catch( ee ) {
            try {
                new ActiveXObject(ia) ;
                ie = true ;
            } catch( e ) {
            }
        }
        var ax =(function(){
            var a ;
            if( ie ) {
                for( var i = 0 ; i < n.length ; i ++ ) {
                    try{
                        new ActiveXObject(iex[i])
                        a = function(){
                            return new ActiveXObject(iex[i])
                        }
                        break ;
                    }catch(e){
                    }
                }
            }
            if( a == _u ) {
                a = function(){
                    return new XMLHttpRequest()
                }
            }
            if( xdom ) {
                return function(d) {
                    if( d == 1 ) {
                        var n = new XDomainRequest()
                        n.ie = 0 ;
                        return n ;
                    }
                    return a() ;
                }
            }
            return a ;
        })();
        
        var head=function(m,x){
            if(m=='POST') {
                x.setRequestHeader('Content-type','application/x-www-form-urlencoded');
            }
        }
        var setHeader=function(x,m){
            if(m) {
                for(var k in m) {
                    x.setRequestHeader(k, m[k]);
                }
            }
        }
        return function( method,url,params,header,call ) {
            method = (method+"").toUpperCase() ;
            url += (( url.indexOf( "?" ) == -1 )? "?":"&" )+(new Date().getTime()) ;
            var pms = "" ;
            if (typeof(params) != "string") {
                for( var k in params ) {
                    pms += "&" + k + "=" + encodeURIComponent( params[ k ] ) ;
                }
            } else {
                pms = params;
            }
            if( method == "GET" ) {
                url += pms ;
                pms = null ;
            }
            if( call == _u ) {
                var x=ax();
                x.open(method,url,false);
                head(method,x);
                setHeader(x,header);
                x.send(pms);
                return x.responseText
            }
            else {
                var x=ax((/^https?:\/\//i.test(url))?1:0);
                if( x.ie == 0 ) {
                    x.onprogress = function() {}
                    x.onload = function() {
                        call(x.status,x.responseText)
                    }
                    x.open(method,url);
                }
                else {
                    x.onreadystatechange=function(){
                        if(x.readyState==4) {
                            call(x.status,x.responseText)
                        }
                    };
                    x.open(method,url,true);
                }
                head(method,x);
                setHeader(x,header);
                x.send(pms)
            }
        };
    })();
    ajax.json = function( v ) {
        return eval("["+v+"]")[0];
    }
    return ajax;
}();

// httpPush受付処理を生成.
// domain 接続先のドメイン(http://sample.com)を設定します.
//
// httpPush受付処理が返却されます.
o.reception = function(domain) {
    if(domain == _u || domain == _n) {
        return null;
    }
    var _j = ajax.json;
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
            res = _j(res);
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
            res = _j(res);
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
            res = _j(res);
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
            res = _j(res);
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
