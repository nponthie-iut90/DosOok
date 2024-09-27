import java.io.*;

public class DosRead {
  static final int FP = 1000;
  static final int BAUDS = 100;
  static final int[] START_SEQ = { 1, 0, 1, 0, 1, 0, 1, 0 };
  FileInputStream fileInputStream;
  int sampleRate = 44100;
  int bitsPerSample;
  int dataSize;
  double[] audio;
  int[] outputBits;
  char[] decodedChars;

  /**
   * Constructor that opens the FIlEInputStream
   * and reads sampleRate, bitsPerSample and dataSize
   * from the header of the wav file
   *
   * @param path the path of the wav file to read
   */
  public void readWavHeader(String path) {
    byte[] header = new byte[44]; // The header is 44 bytes long

    try {
      // Open FileInputStream
      fileInputStream = new FileInputStream(path);
      fileInputStream.read(header);

      // Read header information
      // Read sample rate
      sampleRate = byteArrayToInt(header, 24, 32);
      // Read bits per sample
      bitsPerSample = byteArrayToInt(header, 34, 16);
      // read data size
      dataSize = byteArrayToInt(header, 40, 32);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to convert a little-endian byte array to an integer
   *
   * @param bytes  the byte array to convert
   * @param offset the offset in the byte array
   * @param fmt    the format of the integer (16 or 32 bits)
   * @return the integer value
   */
  private static int byteArrayToInt(byte[] bytes, int offset, int fmt) {
    if (fmt == 16)
      return ((bytes[offset + 1] & 0xFF) << 8) | (bytes[offset] & 0xFF);
    else if (fmt == 32)
      return ((bytes[offset + 3] & 0xFF) << 24) | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 1] & 0xFF) << 8)
          | (bytes[offset] & 0xFF);
    else
      return (bytes[offset] & 0xFF);
  }

  /**
   * Read the audio data from the wav file
   * and convert it to an array of doubles
   * that becomes the audio attribute
   */
  public void readAudioDouble() {
    // Initialization of audioData array
    byte[] audioData = new byte[dataSize];

    try {
      // Read data from audio file
      fileInputStream.read(audioData);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Convert audio data to a double array
    audio = new double[dataSize / 2];
    bitsPerSample = 16; // Number of bytes per sample

    for (int i = 0; i < dataSize / 2; i++) {
      // Convert little-endian to Integer
      int sample = (audioData[2 * i + 1] << 8) | (audioData[2 * i] & 0xFF);
      // standardization between -1 and 1
      audio[i] = sample / 32768.0; //32768.0 ~ MAX_AMP
    }
  }

  /**
   * Reverse the negative values of the audio array
   */
  public void audioRectifier() {
    // for each data from audio array, reverse negative values
    for (int i = 0; i < audio.length; i++) {
      if (audio[i] < 0) {
        audio[i] = -audio[i];
      }
    }
  }

  /**
   * Apply a low pass filter to the audio array
   * Fc = (1/2n)*FECH
   *
   * @param n the number of samples to average
   */
  public void audioLPFilter(int n) {
    // Initialization of the filtered audio array
    double[] filteredAudio = new double[audio.length];

    // Apply low pass filter (moving average)
    for (int i = 0; i < audio.length; i++) {
      double sum = 0.0;
      int start = Math.max(0, i - n + 1);
      int end = i + 1;

      for (int j = start; j < end; j++) {
        sum += audio[j];
      }

      filteredAudio[i] = sum / (double) (end - start);
    }

    // Update the audio array with the filtered values
    audio = filteredAudio;
  }

  /**
   * Resample the audio array and apply a threshold
   *
   * @param period    the number of audio samples by symbol
   * @param threshold the threshold that separates 0 and 1
   */
  public void audioResampleAndThreshold(int period, int threshold) {
    int numSymbols = audio.length / period;
    // Initialize the byte array of converted signal to bits
    outputBits = new int[numSymbols];

    for (int i = 0; i < numSymbols; i++) {
      int start = i * period;
      int end = start + period;

      // Calculate th sum of sample in period
      double sum = 0;
      for (int j = start; j < end; j++) {
        sum += audio[j];
      }

      // MAX_AMP equals to the maximum amplitude from the signal
      int maxAmp = (int) Math.pow(2, bitsPerSample);
      // Calculate the average taking into account the amplitude range
      double average = maxAmp * (sum / period);
      // Apply the threshold, if > threshold : 1, else : 0
      outputBits[i] = (average > threshold) ? 1 : 0;
    }
  }

  /**
   * Decode the outputBits array to a char array
   * The decoding is done by comparing the START_SEQ with the actual beginning of
   * outputBits.
   * The next first symbol is the first bit of the first char.
   */
  public void decodeBitsToChar() {
    int startSeqIndex = -1;
    // Find index of start sequence in outputBits array
    for (int i = 0; i < outputBits.length - START_SEQ.length; i++) {
      boolean match = true;
      for (int j = 0; j < START_SEQ.length; j++) {
        if (outputBits[i + j] != START_SEQ[j]) {
          match = false;
          break;
        }
      }
      if (match) {
        startSeqIndex = i + START_SEQ.length;
        break;
      }
    }

    // if START_SEQ is not found in the file, return an error
    if (startSeqIndex == -1) {
      System.out.println("Start sequence not found in the message.");
      return;
    }

    // Calculate the number of symbols in the message
    int numMessageSymbols = (outputBits.length - startSeqIndex) / 8; // each charachter use 8 bits
    // Initialize decodedChars array
    decodedChars = new char[numMessageSymbols];

    // Decode bits using resampling strategy
    for (int i = 0; i < numMessageSymbols; i++) {
      int start = startSeqIndex + i * 8;
      int end = start + 8; // Use 8 bits for each charachter
      int decodedValue = 0;
      // Convert the 8 bits into an integer (processes bits in reverse order)
      for (int j = end - 1; j >= start; j--) {
        decodedValue = (decodedValue << 1) | outputBits[j];
      }

      // Convert the Integer ro an ASCII charachter
      decodedChars[i] = (char) decodedValue;
    }
  }

  /**
   * Print the elements of an array
   *
   * @param data the array to print
   */
  public static void printIntArray(char[] data) {
    for (char c : data) {
      System.out.print(c);
    }
    System.out.println();
  }

  /**
   * Display a signal in a window
   *
   * @param sig   the signal to display
   * @param start the first sample to display
   * @param stop  the last sample to display
   * @param mode  "line" or "point"
   * @param title the title of the window
   */
  public static void displaySig(double[] sig, int start, int stop, String mode, String title) {
    StdDraw.setCanvasSize(1200, 900);
    StdDraw.setXscale(start, stop);
    StdDraw.setYscale(-1.1, 1.1);
    StdDraw.setTitle(title);

    // Display the x-axis graduation scale
    for (int i = start; i <= stop; i += (stop - start) / 10) {
      StdDraw.text(i, -1.0, String.valueOf(i));
      StdDraw.line(i, -0.02, i, 0.02);
    }

    // Added the blue line representing 0 in the middle of the graphics window
    StdDraw.setPenColor(StdDraw.BLUE);
    StdDraw.setPenRadius(0.005);
    StdDraw.line(start, 0, stop, 0);

    if (mode.equals("line")) {
      // Signal display as line
      for (int i = start; i < stop - 1; i++) {
        StdDraw.line(i, sig[i], i + 1, sig[i + 1]);
      }
    } else if (mode.equals("point")) {
      // Signal display as points
      for (int i = start; i < stop; i++) {
        StdDraw.point(i, sig[i]);
      }
    } else {
      // if unvalid method, print an error
      System.out.println("Unsupported mode");
    }
  }

  /**
   * Un exemple de main qui doit pourvoir être exécuté avec les méthodes
   * que vous aurez conçues.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java DosRead <input_wav_file>");
      return;
    }
    String wavFilePath = args[0];

    // Open the WAV file and read its header
    DosRead dosRead = new DosRead();
    dosRead.readWavHeader(wavFilePath);

    // Print the audio data properties
    System.out.println("Fichier audio: " + wavFilePath);
    System.out.println("\tSample Rate: " + dosRead.sampleRate + " Hz");
    System.out.println("\tBits per Sample: " + dosRead.bitsPerSample + " bits");
    System.out.println("\tData Size: " + dosRead.dataSize + "bytes");

    // Read the audio data
    dosRead.readAudioDouble();
    // reverse the negative values
    dosRead.audioRectifier();
    // apply a low pass filter
    dosRead.audioLPFilter(44);
    // Resample audio data and apply a threshold to output only 0 & 1
    dosRead.audioResampleAndThreshold(dosRead.sampleRate / BAUDS, 12000);
    dosRead.decodeBitsToChar();
    if (dosRead.decodedChars != null) {
      System.out.print("Message décodé : ");
      printIntArray(dosRead.decodedChars);
    }

    displaySig(dosRead.audio, 0, dosRead.audio.length - 1, "line", "Signal audio");

    // Close the file input stream
    try {
      dosRead.fileInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
