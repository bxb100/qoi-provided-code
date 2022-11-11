package cs107;

/**
 * Utility class to manipulate arrays.
 *
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @apiNote First Task of the 2022 Mini Project
 * @since 1.0
 */
public final class ArrayUtils {

	/**
	 * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
	 */
	private ArrayUtils() {
	}

	// ==================================================================================
	// =========================== ARRAY EQUALITY METHODS ===============================
	// ==================================================================================

	/**
	 * Check if the content of both arrays is the same
	 *
	 * @param a1 (byte[]) - First array
	 * @param a2 (byte[]) - Second array
	 * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
	 * @throws AssertionError if one of the parameters is null
	 */
	public static boolean equals(byte[] a1, byte[] a2) {
		if (a1 == null && a2 == null) {
			return true;
		}
		if (a1 == null || a2 == null) {
			throw new AssertionError("one of the parameters is null ");
		}
//		return Arrays.equals(a1, a2);
		if (a1.length != a2.length) {
			return false;
		}
		for (int i = 0; i < a1.length; i++) {
			if (a1[i] != a2[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if the content of both arrays is the same
	 *
	 * @param a1 (byte[][]) - First array
	 * @param a2 (byte[][]) - Second array
	 * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
	 * @throws AssertionError if one of the parameters is null
	 */
	public static boolean equals(byte[][] a1, byte[][] a2) {
		if (a1 == null && a2 == null) {
			return true;
		}
		if (a1 == null || a2 == null) {
			throw new AssertionError("one of the parameters is null");
		}

		if (a1.length != a2.length) {
			return false;
		}

		for (int i = 0; i < a1.length; i++) {

			if (a1[i].length != a2[i].length) {
				return false;
			}

			for (int j = 0; j < a1[i].length; j++) {
				if (a1[i][j] != a2[i][j]) {
					return false;
				}
			}
		}

		return true;
	}

	// ==================================================================================
	// ============================ ARRAY WRAPPING METHODS ==============================
	// ==================================================================================

	/**
	 * Wrap the given value in an array
	 *
	 * @param value (byte) - value to wrap
	 * @return (byte[]) - array with one element (value)
	 */
	public static byte[] wrap(byte value) {
		return new byte[]{value};
	}

	// ==================================================================================
	// ========================== INTEGER MANIPULATION METHODS ==========================
	// ==================================================================================

	/**
	 * Create an Integer using the given array. The input needs to be considered
	 * as "Big Endian"
	 * (See handout for the definition of "Big Endian")
	 *
	 * @param bytes (byte[]) - Array of 4 bytes
	 * @return (int) - Integer representation of the array
	 * @throws AssertionError if the input is null or the input's length is different from 4
	 */
	public static int toInt(byte[] bytes) {

		assert bytes != null && bytes.length == 4;

		int result = 0xFF & bytes[0];
		result <<= 8;
		result += 0xFF & bytes[1];
		result <<= 8;
		result += 0xFF & bytes[2];
		result <<= 8;
		result += 0xFF & bytes[3];
		return result;
	}

	/**
	 * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
	 * (See handout for the definition of "Big Endian")
	 *
	 * @param value (int) - The integer
	 * @return (byte[]) - Big Endian representation of the integer
	 */
	public static byte[] fromInt(int value) {
		byte[] bytes = new byte[4];

		for (int i = 4; i > 0; i--) {
			bytes[i - 1] = (byte) (0xFF & value);
			value >>= 8;
		}

		return bytes;
	}

	// ==================================================================================
	// ========================== ARRAY CONCATENATION METHODS ===========================
	// ==================================================================================

	/**
	 * Concatenate a given sequence of bytes and stores them in an array
	 *
	 * @param bytes (byte ...) - Sequence of bytes to store in the array
	 * @return (byte[]) - Array representation of the sequence
	 * @throws AssertionError if the input is null
	 */
	public static byte[] concat(byte... bytes) {

		assert bytes != null;

		byte[] concat = new byte[bytes.length];

		System.arraycopy(bytes, 0, concat, 0, bytes.length);

		return concat;
	}

	/**
	 * Concatenate a given sequence of arrays into one array
	 *
	 * @param tabs (byte[] ...) - Sequence of arrays
	 * @return (byte[]) - Array representation of the sequence
	 * @throws AssertionError if the input is null
	 *                        or one of the inner arrays of input is null.
	 */
	public static byte[] concat(byte[]... tabs) {
		assert tabs != null;

		int byteLength = 0;
		for (byte[] tab : tabs) {
			byteLength += tab.length;
		}

		byte[] concat = new byte[byteLength];

		int dest = 0;
		for (byte[] bytes : tabs) {
			assert bytes != null;

			System.arraycopy(bytes, 0, concat, dest, bytes.length);
			dest += bytes.length;
		}
		return concat;
	}

	// ==================================================================================
	// =========================== ARRAY EXTRACTION METHODS =============================
	// ==================================================================================

	/**
	 * Extract an array from another array
	 *
	 * @param input  (byte[]) - Array to extract from
	 * @param start  (int) - Index in the input array to start the extract from
	 * @param length (int) - The number of bytes to extract
	 * @return (byte[]) - The extracted array
	 * @throws AssertionError if the input is null or start and length are invalid.
	 *                        start + length should also be smaller than the input's length
	 */
	public static byte[] extract(byte[] input, int start, int length) {

		assert input != null;
		assert start >= 0 && start < input.length;
		assert length >= 0;
		assert (start + length) <= input.length;

		byte[] extract = new byte[length];
		System.arraycopy(input, start, extract, 0, length);
		return extract;
	}

	/**
	 * Create a partition of the input array.
	 * (See handout for more information on how this method works)
	 *
	 * @param input (byte[]) - The original array
	 * @param sizes (int ...) - Sizes of the partitions
	 * @return (byte[][]) - Array of input's partitions.
	 * The order of the partition is the same as the order in sizes
	 * @throws AssertionError if one of the parameters is null
	 *                        or the sum of the elements in sizes is different from the input's length
	 */
	public static byte[][] partition(byte[] input, int... sizes) {
		assert input != null;
		assert sizes != null;

		byte[][] partition = new byte[sizes.length][];
		int start = 0;
		for (int i = 0; i < sizes.length; i++) {
			partition[i] = extract(input, start, sizes[i]);
			start += sizes[i];
		}

		return partition;
	}

	// ==================================================================================
	// ============================== ARRAY FORMATTING METHODS ==========================
	// ==================================================================================

	/**
	 * Format a 2-dim integer array
	 * where each dimension is a direction in the image to
	 * a 2-dim byte array where the first dimension is the pixel
	 * and the second dimension is the channel.
	 * See handouts for more information on the format.
	 *
	 * @param input (int[][]) - image data
	 * @return (byte [][]) - formatted image data
	 * @throws AssertionError if the input is null
	 *                        or one of the inner arrays of input is null
	 */
	public static byte[][] imageToChannels(int[][] input) {
		assert input != null;
		int len = input[0].length;
		for (int[] ints : input) {
			assert len == ints.length;
		}
		// each pixel is 4 byte
		byte[][] channels = new byte[input.length * len][4];
		int i = 0;
		for (int[] ints : input) {
			for (int pixel : ints) {
				// the BufferedImage pixel is argb not rgba
				byte[] channel = fromInt(pixel);
				channels[i][QOISpecification.r] = channel[1];
				channels[i][QOISpecification.g] = channel[2];
				channels[i][QOISpecification.b] = channel[3];
				channels[i][QOISpecification.a] = channel[0];
				i++;
			}
		}
		return channels;
	}

	/**
	 * Format a 2-dim byte array where the first dimension is the pixel
	 * and the second is the channel to a 2-dim int array where the first
	 * dimension is the height and the second is the width
	 *
	 * @param input  (byte[][]) : linear representation of the image
	 * @param height (int) - Height of the resulting image
	 * @param width  (int) - Width of the resulting image
	 * @return (int[][]) - the image data
	 * @throws AssertionError if the input is null
	 *                        or one of the inner arrays of input is null
	 *                        or input's length differs from width * height
	 *                        or height is invalid
	 *                        or width is invalid
	 */
	public static int[][] channelsToImage(byte[][] input, int height, int width) {

		assert input != null;
		assert input[0].length == 4;
		assert input.length == height * width;

		int[][] pixels = new int[height][width];

		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {

				byte[] bytes = input[w + h * width];

				pixels[h][w] = ((bytes[QOISpecification.a] & 0xFF) << 24)
						| ((bytes[QOISpecification.r] & 0xFF) << 16)
						| ((bytes[QOISpecification.g] & 0xFF) << 8)
						| (bytes[QOISpecification.b] & 0xFF);
			}
		}

		return pixels;
	}

}
