
package tesseract;

import org.opencv.core.Core;

import gui.WindowManager;

public class TesseractClass {

	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		WindowManager windowManager = new WindowManager();
	}
}
