package com.github.hypfvieh.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import com.github.hypfvieh.common.SearchOrder;
import com.github.hypfvieh.util.FileIoUtil;

/**
 * Class to read a Textfile in a {@link Stream}&lt;String&gt; and do stream operations on it.
 *
 * @author hypfvieh
 * @since v1.0.1 - 2017-10-12
 */
public final class TextFileStream {

	private TextFileStream() {

	}

	public static Stream<String> readFileToStream(String _inputFile, Charset _charset) {
		return readFileToStream(FileIoUtil.openInputStreamForFile(_inputFile, SearchOrder.CUSTOM_PATH, SearchOrder.CLASS_PATH), _charset);
	}

	public static Stream<String> readFileToStream(File _inputFile, Charset _charset) {
		return readFileToStream(FileIoUtil.openInputStreamForFile(_inputFile.getAbsolutePath(), SearchOrder.CUSTOM_PATH, SearchOrder.CLASS_PATH), _charset);
	}

	/**
	 * Returns the file as {@link java.util.stream.Stream}.<br>
	 *
	 * @param _input InputStream to read
	 * @param _charset Charset to use
	 * @return Stream
	 */
	public static Stream<String> readFileToStream(InputStream _input, Charset _charset) {
		return new BufferedReader(new InputStreamReader(_input, _charset.newDecoder())).lines();
	}

}
