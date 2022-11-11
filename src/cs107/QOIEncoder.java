package cs107;

/**
 * "Quite Ok Image" Encoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @apiNote Second task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIEncoder {

	/**
	 * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
	 */
	private QOIEncoder() {
	}

	// ==================================================================================
	// ============================ QUITE OK IMAGE HEADER ===============================
	// ==================================================================================

	/**
	 * Generate a "Quite Ok Image" header using the following parameters
	 *
	 * @param image (Helper.Image) - Image to use
	 * @return (byte[]) - Corresponding "Quite Ok Image" Header
	 * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
	 *                        (See the "Quite Ok Image" Specification or the handouts of the project for more information)
	 */
	public static byte[] qoiHeader(Helper.Image image) {
		assert image != null;
		assert image.channels() == QOISpecification.RGB || image.channels() == QOISpecification.RGBA;
		assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;

		return ArrayUtils.concat(
				QOISpecification.QOI_MAGIC,
				// width
				ArrayUtils.fromInt(image.data()[0].length),
				// height
				ArrayUtils.fromInt(image.data().length),
				// channels
				ArrayUtils.wrap(image.channels()),
				// colorspace
				ArrayUtils.wrap(image.color_space())
		);
	}

	// ==================================================================================
	// ============================ ATOMIC ENCODING METHODS =============================
	// ==================================================================================

	/**
	 * Encode the given pixel using the QOI_OP_RGB schema
	 *
	 * @param pixel (byte[]) - The Pixel to encode
	 * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
	 * @throws AssertionError if the pixel's length is not 4
	 */
	public static byte[] qoiOpRGB(byte[] pixel) {
		assert pixel.length == 4;

		return ArrayUtils.concat(
				QOISpecification.QOI_OP_RGB_TAG,
				pixel[0],
				pixel[1],
				pixel[2]
		);
	}

	/**
	 * Encode the given pixel using the QOI_OP_RGBA schema
	 *
	 * @param pixel (byte[]) - The pixel to encode
	 * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
	 * @throws AssertionError if the pixel's length is not 4
	 */
	public static byte[] qoiOpRGBA(byte[] pixel) {
		assert pixel.length == 4;

		return ArrayUtils.concat(
				ArrayUtils.wrap(QOISpecification.QOI_OP_RGBA_TAG),
				pixel
		);
	}

	/**
	 * Encode the index using the QOI_OP_INDEX schema
	 *
	 * @param index (byte) - Index of the pixel
	 * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
	 * @throws AssertionError if the index is outside the range of all possible indices
	 */
	public static byte[] qoiOpIndex(byte index) {
		assert index <= 63 && index >= 0;

		return ArrayUtils.wrap((byte) (QOISpecification.QOI_OP_INDEX_TAG | index));
	}

	/**
	 * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
	 *
	 * @param diff (byte[]) - The difference between 2 pixels
	 * @return (byte[]) - Encoding of the given difference
	 * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
	 *                        (See the handout for the constraints)
	 */
	public static byte[] qoiOpDiff(byte[] diff) {

		assert diff != null;
		assert diff.length >= 3;
		assert diff[0] > -3 && diff[0] < 2;
		assert diff[1] > -3 && diff[1] < 2;
		assert diff[2] > -3 && diff[2] < 2;

		return ArrayUtils.wrap((byte) (QOISpecification.QOI_OP_DIFF_TAG
				| (diff[0] + 2) << 4
				| (diff[1] + 2) << 2
				| (diff[2] + 2)
		));
	}

	/**
	 * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
	 *
	 * @param diff (byte[]) - The difference between 2 pixels
	 * @return (byte[]) - Encoding of the given difference
	 * @throws AssertionError if diff doesn't respect the constraints
	 *                        or diff's length is not 3
	 *                        (See the handout for the constraints)
	 */
	public static byte[] qoiOpLuma(byte[] diff) {

		assert diff != null;
		assert diff.length >= 3;

		byte dr = diff[0];
		byte dg = diff[1];
		byte db = diff[2];
		byte dr_dg = (byte) (dr - dg);
		byte db_dg = (byte) (db - dg);

		assert smallDiff(dg);
		assert smallerDiff(dr_dg);
		assert smallerDiff(db_dg);

		byte[] byte0 = ArrayUtils.wrap((byte) (QOISpecification.QOI_OP_LUMA_TAG | (dg + 32)));
		byte[] byte1 = ArrayUtils.wrap((byte) ((dr_dg + 8) << 4 | (db_dg + 8)));
		return ArrayUtils.concat(byte0, byte1);
	}

	/**
	 * Encode the number of similar pixels using the QOI_OP_RUN schema
	 *
	 * @param count (byte) - Number of similar pixels
	 * @return (byte[]) - Encoding of count
	 * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
	 */
	public static byte[] qoiOpRun(byte count) {

		return ArrayUtils.wrap(
				(byte) (QOISpecification.QOI_OP_RUN_TAG | (count - 1))
		);
	}

	// ==================================================================================
	// ============================== GLOBAL ENCODING METHODS  ==========================
	// ==================================================================================

	/**
	 * Encode the given image using the "Quite Ok Image" Protocol
	 * (See handout for more information about the "Quite Ok Image" protocol)
	 *
	 * @param image (byte[][]) - Formatted image to encode
	 * @return (byte[]) - "Quite Ok Image" representation of the image
	 */
	public static byte[] encodeData(byte[][] image) {
		// step 1 initialization
		byte[] previousPixel = QOISpecification.START_PIXEL;
		byte[][] indexHashTable = new byte[64][4];
		byte counter = 0;

		// step 2 pixel process
		byte[] result = new byte[0];
		for (int i = 0; i < image.length; i++) {
			byte[] pixel = image[i];
			// 1.
			if (ArrayUtils.equals(pixel, previousPixel)) {
				counter++;
				if (counter >= 62 || i == image.length - 1) {
					result = ArrayUtils.concat(result, qoiOpRun(counter));
					counter = 0;
				}
				previousPixel = pixel;
				continue;
			}
			if (counter != 0) {
				result = ArrayUtils.concat(result, qoiOpRun(counter));
				counter = 0;
			}
			// 2.
			byte index = QOISpecification.hash(pixel);
			if (ArrayUtils.equals(indexHashTable[index], pixel)) {
				result = ArrayUtils.concat(result, qoiOpIndex(index));
				previousPixel = pixel;
				continue;
			}
			indexHashTable[index] = pixel;
			// 3.
			if (pixel[3] == previousPixel[3]) {
				byte dr = (byte) (pixel[0] - previousPixel[0]);
				byte dg = (byte) (pixel[1] - previousPixel[1]);
				byte db = (byte) (pixel[2] - previousPixel[2]);
				byte dr_dg = (byte) (dr - dg);
				byte db_dg = (byte) (db - dg);
				if (smallestDiff(dr) && smallestDiff(dg) && smallestDiff(db)) {
					// 3
					result = ArrayUtils.concat(result, qoiOpDiff(new byte[]{dr, dg, db}));
				} else if (smallDiff(dg) && smallerDiff(dr_dg) && smallerDiff(db_dg)) {
					// 4
					result = ArrayUtils.concat(result, qoiOpLuma(new byte[]{dr, dg, db}));
				} else {
					// 5
					result = ArrayUtils.concat(result, qoiOpRGB(pixel));
				}
			} else {
				// 6
				result = ArrayUtils.concat(result, qoiOpRGBA(pixel));
			}
			previousPixel = pixel;
		}

		return result;
	}

	private static boolean smallDiff(int i) {
		return i > -33 && i < 32;
	}

	private static boolean smallerDiff(int i) {
		return i > -9 && i < 8;
	}

	private static boolean smallestDiff(int i) {
		return i > -3 && i < 2;
	}

	/**
	 * Creates the representation in memory of the "Quite Ok Image" file.
	 *
	 * @param image (Helper.Image) - Image to encode
	 * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
	 * @throws AssertionError if the image is null
	 * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
	 * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
	 */
	public static byte[] qoiFile(Helper.Image image) {

		byte[] header = qoiHeader(image);
		byte[] content = encodeData(ArrayUtils.imageToChannels(image.data()));

		return ArrayUtils.concat(header, content, QOISpecification.QOI_EOF);
	}

}
