package com.maachang.httpPush.net.http;

public class HttpStatus {
    private HttpStatus() {
    }

    /**
     * HTTPステータスに対するメッセージを取得.
     * 
     * @param state
     *            対象のHTTPステータスを設定します.
     * @return String HTTPステータスに対するメッセージが返却されます.
     */
    public static final String getMessage(int state) {
        switch (state) {
        // リクエスト処理中ステータス.
        case 100:
            return "Continue"; // 要求は続行可能です.
        case 101:
            return "Switching Protocols"; // サーバによって更新ヘッダのプロトコルが切り替えられました.

            // リクエスト正常系スタータス.
        case 200:
            return "OK"; // 要求は正常に終了しました.
        case 201:
            return "Created"; // 要求は満足され、新規リソースが作成されました.
        case 202:
            return "Accepted"; // 処理するために要求が受け付けられましたが、その処理は完了していません.
        case 203:
            return "Non-Authoritative Information"; // エンティティヘッダに返されたメタ情報は、元のサーバから入手できる完全な
            // セットではありません.
        case 204:
            return "No Content"; // サーバは要求を処理しましたが、送り返す新規の情報がありません.
        case 205:
            return "Reset Content"; // 要求は完了しました。クライアント プログラムは要求の送信元であるドキュメント
            // ビューをリセットして、ユーザが次の入力操作をできるようにする必要があります
        case 206:
            return "Partial Content"; // サーバによってリソースの GET 要求の一部が処理されました.

            // リクエスト再処理要求系ステータス.
        case 300:
            return "Multiple Choices"; // サーバから何を返すか判断できませんでした.
        case 301:
            return "Moved Permanently"; // 要求された情報が Location ヘッダで指定される URI
            // に移動したことを示します。
            // このステータスを受信したときの既定のアクションは、応答に関連付けられている
            // Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
            // 要求は GET メソッドを使用します
        case 302:
            return "Moved Temporarily"; // 要求された情報が Location ヘッダで指定される URI
            // にあることを示します。
            // このステータスを受信したときの既定のアクションは、応答に関連付けられている
            // Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
            // 要求は GET メソッドを使用します
        case 303:
            return "See Other"; // POST の結果として、Location ヘッダで指定された URI
            // にクライアントを自動的に
            // リダイレクトします。Location ヘッダで指定されるリソースへの要求は、GET で行います
        case 304:
            return "Not Modified"; // クライアントのキャッシュされたコピーが最新のものであることを示します。
            // リソースの内容は転送されません.
        case 305:
            return "Use Proxy"; // 要求が Location ヘッダで指定される URI でプロキシ
            // サーバを使用する必要があることを示します
        case 307:
            return "Temporary Redirect"; // 要求された情報が Location ヘッダで指定される URI
            // にあることを示します。
            // このステータスを受信したときの既定のアクションは、応答に関連付けられている
            // Location ヘッダの追跡です。元の要求メソッドが POST の場合、リダイレクトされた
            // 要求も POST メソッドを使用します

            // リクエスト警告系ステータス.
        case 400:
            return "Bad Request"; // 無効な要求です.
        case 401:
            return "Authorization Required"; // 要求されたリソースには、ユーザの認証が必要です.
        case 402:
            return "Payment Required"; // 支払いが必要です.(※未実装).
        case 403:
            return "Forbidden"; // 要求はサーバで解読されましたが、その処理は拒否されました.
        case 404:
            return "Not Found"; // 要求されたリソースがサーバに存在していないことを示します.
        case 405:
            return "Method Not Allowed"; // 要求メソッド (POST または GET)
            // が要求リソースで許可されていないことを示します.
        case 406:
            return "Not Acceptable"; // クライアントが Accept
            // ヘッダでリソースの利用可能な任意の表現を受け入れないことを
            // 指定していることを示します
        case 407:
            return "Proxy Authentication Required"; // プロキシによる認証が必要です.
        case 408:
            return "Request Time-out"; // 要求待ちでサーバがタイムアウトしました.
        case 409:
            return "Conflict"; // リソースの現在の状態と矛盾するため、要求は完了できませんでした。
            // 詳しい情報を再度送信する必要があります
        case 410:
            return "Gone"; // 要求されたリソースはサーバにありません。転送先アドレスは不明です.
        case 411:
            return "Length Required"; // 内容の長さが定義されていない要求の受け入れをサーバが拒否しました.
        case 412:
            return "Precondition Failed"; // 要求の 1 つ以上のヘッダ
            // フィールドにある事前条件がサーバでテストされ、不正と判定されました
        case 413:
            return "Request Entity Too Large"; // 要求が大きすぎて、サーバで処理できないことを示します.
        case 414:
            return "Request-URI Too Large"; // 要求された URI が長すぎます.
        case 415:
            return "Unsupported Media Type"; // サポートされていないメディアの種類です.
        case 416:
            return "Requested range not satisfiable"; // 要求された範囲内にありません.
        case 417:
            return "Expectation Failed"; // サーバが Expect
            // ヘッダで指定された要求を満たすことができないことを示します.

            // リクエストエラー系ステータス.
        case 500:
            return "Internal Server Error"; // サーバで一般的なエラーが発生したことを示します.
        case 501:
            return "Not Implemented"; // サーバが要求された機能をサポートしていないことを示します.
        case 502:
            return "Bad Gateway"; // 中間プロキシ
            // サーバが別のプロキシまたは元のサーバから無効な応答を受け取ったことを示します.
        case 503:
            return "Service Unavailable"; // 高い負荷または保守のため、サーバを一時的に利用できないことを示します.
        case 504:
            return "Gateway Time-out"; // ゲートウェイ待ちで要求がタイムアウトしました.
        case 505:
            return "HTTP Version not supported"; // サポートされていない HTTP のバージョンです
        }
        return "no support status code(" + state + ")";
    }
}
