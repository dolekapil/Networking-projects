import java.net.InetAddress;

/**
 * Project #2 This the main class and used for parsing the arguments.
 *
 * @author Kapil Dole
 */
public class fcntcp {
	public static void main(String[] args) {
		// Program terminates if arguments entered are invalid.
		if (!processArguments(args)) {
			System.out.println("Invalid Arguments! Please try again..");
			System.exit(1);
		}
	}

	/**
	 * This method basically process arguments provided by the user.
	 * 
	 * @param args
	 *            arguments provided by user.
	 */
	public static boolean processArguments(String[] args) {
		// Based on the argument call appropriate function for processing it.
		if (args[0].equals("-c") || args[0].equals("--client")) {
			return processClient(args);
		} else if (args[0].equals("-s") || args[0].equals("--server")) {
			return processServer(args);
		} else {
			return false;
		}
	}

	/**
	 * This method is used for processing client option.
	 * 
	 * @param args
	 *            arguments provided by user.
	 * 
	 * @return boolean true if success, else false.
	 */
	public static boolean processClient(String[] args) {
		String fileName = "", serverAddress = args[args.length - 2];
		int timeOut = 1000, port = Integer.parseInt(args[args.length - 1]), index = 1;
		boolean quiet = false;
		try {
			while (index < args.length - 2) {
				if (args[index].equals("-f") || args[index].equals("--file")) {
					fileName = args[index + 1];
					index += 2;
				} else if (args[index].equals("-t") || args[index].equals("--timeout")) {
					timeOut = Integer.parseInt(args[index + 1]);
					index += 2;
				} else if (args[index].equals("-q") || args[index].equals("--quiet")) {
					quiet = true;
					index++;
				} else {
					index++;
				}
			}
			if (fileName.equals("")) {
				throw new Exception("Plese provide the file name.");
			}
			UDPClient client = new UDPClient(fileName, port, timeOut, InetAddress.getByName(serverAddress), quiet);
			client.startClient();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * This method is used for processing server option.
	 * 
	 * @return boolean true if success, else false.
	 */
	public static boolean processServer(String[] args) {
		int index = 1, port = Integer.parseInt(args[args.length - 1]);
		int timeOut = 1000;
		boolean quiet = false;
		while (index < args.length - 1) {
			if (args[index].equals("-t") || args[index].equals("--timeout")) {
				timeOut = Integer.parseInt(args[index + 1]);
				index += 2;
			} else if (args[index].equals("-q") || args[index].equals("--quiet")) {
				quiet = true;
				index++;
			} else {
				index++;
			}
		}
		UDPServer server = new UDPServer(port, timeOut, quiet);
		server.startServer();
		return true;
	}
}
