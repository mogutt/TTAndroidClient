package com.mogujie.widget.imageview;

import com.squareup.picasso.Transformation;

/**
 * 
 * @author dolphinWang
 * @time 2014/03/10
 */
public class BitmapUtils {
	public enum Effection {
		CIRCLE("circle"), ROUND_CORNER("rounded_corner");

		private String effect;

		private Effection(String effect) {
			this.effect = effect;
		}

		public boolean equals(Effection effection) {
			return this.effect.equals(effection.effect);
		}
	}

	private static final int DEFAULT_CORNER_SIZE = 10;

	private BitmapUtils() {
	}

	public static Transformation get(Effection effection) {
		if (effection == Effection.CIRCLE) {
			return new CircleTransfrom();
		} else if (effection == Effection.ROUND_CORNER) {
			return new RoundedCornerTransfrom(DEFAULT_CORNER_SIZE);
		}

		return null;
	}

	public static Transformation get(String effection) {
		return get(Effection.valueOf(effection));
	}

	public static Transformation get(int effection) {
		if (effection < 0) {
			return null;
		}

		return get(Effection.values()[effection]);
	}

}
