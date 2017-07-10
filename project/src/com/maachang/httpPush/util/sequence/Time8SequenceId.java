package com.maachang.httpPush.util.sequence;

import java.util.concurrent.atomic.AtomicLong;

/**
 * SnowFlakeを改良したシーケンスID生成器. SnowFlakeとは、時間を利用したシーケンスIDを生成するための ロジック。
 * 利点としては、分散環境でのシーケンスID生成に適していること。 時間軸で情報生成するので、並び順で利用するDBキーの場合に適していること。
 * ファイルで永続化せずとも、時間軸で生成されるので、オンメモリで処理できること。
 * 欠点としては、時間が重要になるため、NTPサーバなどで時間調整するが、このとき、
 * サーバ側の時間が巻き戻った場合に、シーケンスIDの生成がストップすること。 ８１９２個のシーケンスIDは１ミリ秒での最大の生成となり、これを超えると、次の
 * １ミリ秒を待たなければならないこと。
 */
public class Time8SequenceId {

	/** 最大マシーンID. **/
	protected static final int MAX_MACHINE_ID = 511;

	protected static final long BASE_TIME = 1483228799000L;
	protected static final long TIME_MASK = 0x7fffffffffc00000L;
	protected static final long SEQ_MASK = 0x0000000000001fffL;

	protected final long baseTime;
	protected final long machineId;
	protected final AtomicLong nowId = new AtomicLong(-1L);
	protected volatile long beforeId = -1L;

	/**
	 * コンストラクタ.
	 */
	protected Time8SequenceId() {
		baseTime = BASE_TIME;
		machineId = 0L;
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します. この値の最大値は、0から511までです.
	 */
	public Time8SequenceId(int id) {
		this(-1L, id);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param base
	 *            対象の基本時間を設定します.
	 * @param id
	 *            対象のマシンIDを設定します. この値の最大値は、0から511までです.
	 */
	public Time8SequenceId(long base, int id) {
		id = check(id);
		if (base <= 0L) {
			base = BASE_TIME;
		}
		baseTime = base;
		machineId = (long) id;
		nowId.set(((System.currentTimeMillis() - baseTime) << 22L)
				| (machineId << 13L));
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param id
	 *            対象のマシンIDを設定します. この値の最大値は、0から511までです.
	 * @param lastId
	 *            設定した最終IDを設定します.
	 */
	public Time8SequenceId(int id, long lastId) {
		this(-1L, id, lastId);
	}

	/**
	 * コンストラクタ.
	 * 
	 * @param base
	 *            対象の基本時間を設定します.
	 * @param id
	 *            対象のマシンIDを設定します. この値の最大値は、0から511までです.
	 * @param lastId
	 *            設定した最終IDを設定します.
	 */
	public Time8SequenceId(long base, int id, long lastId) {
		id = check(id);
		if (base <= 0L) {
			base = BASE_TIME;
		}
		baseTime = base;
		machineId = (long) id;
		set(lastId);
	}

	/** ID範囲チェック. **/
	public static final int check(int id) {
		return (id < 0) ? 0 : ((id > MAX_MACHINE_ID) ? MAX_MACHINE_ID : id);
	}

	/**
	 * シーケンスIDを発行.
	 * 
	 * @return long シーケンスIDが返却されます.
	 */
	public final long get() {
		long now, seq, ret;

		// AtomicでシーケンスIDを生成する。
		// そのため、更新失敗時のループ実装.
		while (true) {
			now = (System.currentTimeMillis() - baseTime) << 22L;
			seq = nowId.get();

			// 新しい時間の場合は、シーケンスIDをゼロクリア.
			if (now > (seq & TIME_MASK)) {

				// 新しいIDをセット.
				if (nowId.compareAndSet(seq,
						(ret = now | (machineId << 13L)) | 1L)) {
					beforeId = ret;
					return ret;
				}
			}
			// ミリ秒毎の最大シーケンスIDを超えた場合
			// 現在の時間にミリ秒１プラスして、シーケンスIDをゼロクリア.
			else if ((seq & SEQ_MASK) == SEQ_MASK) {

				// 現在時間に＋１ミリ秒設定した値で、生成.
				if (nowId.compareAndSet(seq, (ret = (seq & TIME_MASK)
						+ (1L << 22L) | (machineId << 13L)) | 1L)) {
					beforeId = ret;
					return ret;
				}
			}
			// シーケンスIDを１プラス.
			else if (nowId.compareAndSet(seq, (seq & TIME_MASK)
					| (machineId << 13L) | ((seq + 1) & SEQ_MASK))) {
				beforeId = seq;
				return seq;
			}
		}
	}

	/**
	 * 現在のシーケンスIDを収得.
	 * 
	 * @return long 現在のシーケンスIDが返却されます. また１度も取得していない場合は-1が返却されます.
	 */
	public final long now() {
		return beforeId;
	}

	/**
	 * シーケンス情報をセット.
	 * 
	 * @param id
	 *            対象のIDを設定します.
	 */
	public final void set(long id) {
		nowId.set(((id & TIME_MASK) + (1L << 22L)) | (machineId << 13L));
	}

	/**
	 * 基本時間を取得.
	 * 
	 * @return long 設定されている基本時間が返却されます.
	 */
	public final long getBaseTime() {
		return baseTime;
	}

	/**
	 * マシンIDを取得.
	 * 
	 * @return int 設定されているマシンIDが返却されます.
	 */
	public final int getMachineId() {
		return (int) machineId;
	}
}
