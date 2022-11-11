package cs107;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @apiNote Third task of the 2022 Mini Project
 * @since 1.0
 */
public final class QOIDecoder {

	/**
	 * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
	 */
	private QOIDecoder() {
	}

	// ==================================================================================
	// =========================== QUITE OK IMAGE HEADER ================================
	// ==================================================================================

	/**
	 * Extract useful information from the "Quite Ok Image" header
	 *
	 * @param header (byte[]) - A "Quite Ok Image" header
	 * @return (int[]) - Array such as its content is {width, height, channels, color space}
	 * @throws AssertionError See handouts section 6.1
	 */
	public static int[] decodeHeader(byte[] header) {
		assert header != null;
		assert header.length == QOISpecification.HEADER_SIZE;

		byte[][] partition = ArrayUtils.partition(header, 4, 4, 4, 1, 1);

		assert ArrayUtils.equals(partition[0], QOISpecification.QOI_MAGIC);

		int width = ArrayUtils.toInt(partition[1]);
		int height = ArrayUtils.toInt(partition[2]);
		int channels = partition[3][0];

		assert channels == QOISpecification.RGB || channels == QOISpecification.RGBA;

		int colorspace = partition[4][0];

		assert colorspace == QOISpecification.ALL || colorspace == QOISpecification.sRGB;
		return new int[]{width, height, channels, colorspace};
	}

	// ==================================================================================
	// =========================== ATOMIC DECODING METHODS ==============================
	// ==================================================================================

	/**
	 * Store the pixel in the buffer and return the number of consumed bytes
	 *
	 * @param buffer   (byte[][]) - Buffer where to store the pixel
	 * @param input    (byte[]) - Stream of bytes to read from
	 * @param alpha    (byte) - Alpha component of the pixel
	 * @param position (int) - Index in the buffer
	 * @param idx      (int) - Index in the input
	 * @return (int) - The number of consumed bytes
	 * @throws AssertionError See handouts section 6.2.1
	 */
	public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx) {
		assert buffer != null;
		assert input != null;
		assert idx >= 0 && idx < input.length;
		assert input.length - idx >= 3;

		// 0b11111110(not contain) + r + g + b
		buffer[position] = new byte[]{input[idx++], input[idx++], input[idx], alpha};
		return 3;
	}

	/**
	 * Store the pixel in the buffer and return the number of consumed bytes
	 *
	 * @param buffer   (byte[][]) - Buffer where to store the pixel
	 * @param input    (byte[]) - Stream of bytes to read from
	 * @param position (int) - Index in the buffer
	 * @param idx      (int) - Index in the input
	 * @return (int) - The number of consumed bytes
	 * @throws AssertionError See handouts section 6.2.2
	 */
	public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx) {
		assert buffer != null;
		assert input != null;
		assert idx >= 0 && idx < input.length;
		assert input.length - idx >= 4;

		buffer[position] = new byte[]{input[idx++], input[idx++], input[idx++], input[idx]};
		return 4;
	}

	/**
	 * Create a new pixel following the "QOI_OP_DIFF" schema.
	 *
	 * @param previousPixel (byte[]) - The previous pixel
	 * @param chunk         (byte) - A "QOI_OP_DIFF" data chunk
	 * @return (byte[]) - The newly created pixel
	 * @throws AssertionError See handouts section 6.2.4
	 */
	public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk) {

		assert previousPixel != null;
		assert previousPixel.length == 4;
		assert (chunk & QOISpecification.QOI_OP_DIFF_TAG) == QOISpecification.QOI_OP_DIFF_TAG;

		// cur - pre + 2 = chunk

		byte dr = (byte) ((chunk >> 4 & 0x3) - 2 + previousPixel[0]);
		byte dg = (byte) ((chunk >> 2 & 0x3) - 2 + previousPixel[1]);
		byte db = (byte) ((chunk & 0x3) - 2 + previousPixel[2]);

		return new byte[]{dr, dg, db, previousPixel[3]};
	}

	/**
	 * Create a new pixel following the "QOI_OP_LUMA" schema
	 *
	 * @param previousPixel (byte[]) - The previous pixel
	 * @param data          (byte[]) - A "QOI_OP_LUMA" data chunk
	 * @return (byte[]) - The newly created pixel
	 * @throws AssertionError See handouts section 6.2.5
	 */
	public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data) {

		assert previousPixel != null;
		assert previousPixel.length == 4;
		assert ((data[0] & QOISpecification.QOI_OP_LUMA_TAG) == QOISpecification.QOI_OP_LUMA_TAG);

		byte dg = (byte) ((data[0] ^ QOISpecification.QOI_OP_LUMA_TAG) - 32);
		byte dr = (byte) (((data[1] & 0xF0) >> 4) - 8 + dg);
		byte db = (byte) ((data[1] & 0xF) - 8 + dg);

		return new byte[]{
				(byte) (dr + previousPixel[0]),
				(byte) (dg + previousPixel[1]),
				(byte) (db + previousPixel[2]),
				previousPixel[3]
		};
	}

	/**
	 * Store the given pixel in the buffer multiple times
	 *
	 * @param buffer   (byte[][]) - Buffer where to store the pixel
	 * @param pixel    (byte[]) - The pixel to store
	 * @param chunk    (byte) - a QOI_OP_RUN data chunk
	 * @param position (int) - Index in buffer to start writing from
	 * @return (int) - number of written pixels in buffer
	 * @throws AssertionError See handouts section 6.2.6
	 */
	public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position) {
		assert buffer != null;
		assert position >= 0 && position < buffer.length;
		assert pixel != null;
		assert pixel.length == 4;

		// bias -1
		int counter = (chunk ^ QOISpecification.QOI_OP_RUN_TAG) + 1;

		assert buffer.length >= counter;

		for (int i = 0; i < counter; i++) {
			buffer[position + i] = pixel;
		}
		return counter - 1;
	}

	// ==================================================================================
	// ========================= GLOBAL DECODING METHODS ================================
	// ==================================================================================

	/**
	 * Decode the given data using the "Quite Ok Image" Protocol
	 *
	 * @param data   (byte[]) - Data to decode
	 * @param width  (int) - The width of the expected output
	 * @param height (int) - The height of the expected output
	 * @return (byte[][]) - Decoded "Quite Ok Image"
	 * @throws AssertionError See handouts section 6.3
	 */
	public static byte[][] decodeData(byte[] data, int width, int height) {

		assert data != null;
		assert width > 0 && height > 0;
		assert data.length >= width * height;

		byte[][] tab = new byte[width * height][4];

		// step 1 initialization
		byte[] previousPixel = QOISpecification.START_PIXEL;
		byte[][] indexHashTable = new byte[64][4];

		// step 2
		int position = 0;
		for (int idx = 0; idx < data.length; idx++) {

			if (data[idx] == QOISpecification.QOI_OP_RGB_TAG) {
				// ps. first is tag
				idx += decodeQoiOpRGB(tab, data, previousPixel[3], position, idx + 1);
			} else if (data[idx] == QOISpecification.QOI_OP_RGBA_TAG) {
				idx += decodeQoiOpRGBA(tab, data, position, idx + 1);
			} else {
				byte tag = (byte) (data[idx] & 0xC0);
				if (tag == QOISpecification.QOI_OP_DIFF_TAG) {
					tab[position] = decodeQoiOpDiff(previousPixel, data[idx]);
				} else if (tag == QOISpecification.QOI_OP_LUMA_TAG) {
					tab[position] = decodeQoiOpLuma(previousPixel, new byte[]{data[idx++], data[idx]});
				} else if (tag == QOISpecification.QOI_OP_RUN_TAG) {
					int c = decodeQoiOpRun(tab, previousPixel, data[idx], position);
					position += c;
				} else {
					tab[position] = indexHashTable[data[idx] & 0x3F];
				}
			}
			previousPixel = tab[position++];
			indexHashTable[QOISpecification.hash(previousPixel)] = previousPixel;
		}

		return tab;
	}

	/**
	 * Decode a file using the "Quite Ok Image" Protocol
	 *
	 * @param content (byte[]) - Content of the file to decode
	 * @return (Image) - Decoded image
	 * @throws AssertionError if content is null
	 */
	public static Image decodeQoiFile(byte[] content) {
		assert content != null;
		assert ArrayUtils.equals(ArrayUtils.extract(content, content.length - QOISpecification.QOI_EOF.length, QOISpecification.QOI_EOF.length), QOISpecification.QOI_EOF);

		byte[] header = ArrayUtils.extract(content, 0, QOISpecification.HEADER_SIZE);
		int[] headers = decodeHeader(header);

		byte[][] bytes = decodeData(
				ArrayUtils.extract(content, QOISpecification.HEADER_SIZE, content.length - QOISpecification.HEADER_SIZE - QOISpecification.QOI_EOF.length),
				headers[0], headers[1]
		);

		int[][] channelsToImage = ArrayUtils.channelsToImage(bytes, headers[1], headers[0]);
		return new Image(channelsToImage, (byte) headers[2], (byte) headers[3]);
	}

}
