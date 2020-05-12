package lpi.server.mq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

/**
 * @author RST The class that describes a file that should be transferred to
 *         some user.
 */
public class FileInfo implements Serializable {
	private static final long serialVersionUID = 8407920676195680991L;

	private String sender;
	private String receiver;
	private String filename;
	private byte[] fileContent;

	/**
	 * Constructs an empty File Info object. Should be used mainly for
	 * serialization purposes.
	 */
	public FileInfo() {
	}

	/**
	 * Constructs a File Info object with the receiver and the file
	 * specified.
	 * 
	 * @param receiver
	 *            The receiver that the file should be delivered to.
	 * @param file
	 *            The <b>File</b> object that points to a file that should
	 *            be transferred to receiver.
	 * @throws IOException
	 *             if the system failed to read or process the file.
	 */
	public FileInfo(String receiver, File file) throws IOException {
		this.receiver = receiver;
		this.filename = file.getName();
		this.fileContent = Files.readAllBytes(file.toPath());
	}

	/**
	 * Constructs a File Info object with the receiver, sender, filename and
	 * content specified.
	 * 
	 * @param receiver
	 *            The receiver that the file should be delivered to.
	 * @param sender
	 *            The sender that is sending this file.
	 * @param filename
	 *            The name of the file that should be transferred (usually
	 *            just a file name without a path).
	 * @param content
	 *            The content of the file to send.
	 */
	public FileInfo(String receiver, String sender, String filename, byte[] content) {
		this.sender = sender;
		this.receiver = receiver;
		this.filename = filename;
		this.fileContent = content;
	}

	/**
	 * Gets the sender of the file.
	 * 
	 * @return A <b>String</b> that defines the sender of the file.
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Sets the sender of the file.
	 * 
	 * @param sender
	 *            A <b>String</b> that defines the sender of the file.
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Gets the receiver of the file.
	 * 
	 * @return A <b>String</b> that defines the receiver of the file.
	 */
	public String getReceiver() {
		return receiver;
	}

	/**
	 * Sets the receiver of the file.
	 * 
	 * @param receiver
	 *            A <b>String</b> that defines the receiver of the file.
	 */
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	/**
	 * Gets the name of the file.
	 * 
	 * @return A <b>String</b> that defines a name of the file (only the
	 *         file name, without the file path).
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the name of the file.
	 * 
	 * @param filename
	 *            A <b>String</b> that defines the name of the file (only
	 *            the file name, without the file path).
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the content of the file.
	 * 
	 * @return An array of bytes that define the content of the file.
	 */
	public byte[] getFileContent() {
		return fileContent;
	}

	/**
	 * Sets the content of the file.
	 * 
	 * @param fileContent
	 *            An array of bytes that define the content of the file.
	 */
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	/**
	 * Saves the file to a specified location.
	 * 
	 * @param location
	 *            A <b>File</b> that specifies either the existing folder or
	 *            the full path where the file should be saved. In case the
	 *            complete filename is specified, the file will be
	 *            rewritten.
	 * @throws IOException
	 *             If the system failed to save the file to a specified
	 *             location.
	 */
	public void saveFileTo(File location) throws IOException {
		File fileLocation = location;

		// checking if we received a directory or a file.
		if (!location.isFile()) {
			// yep, that's a wood^w directory.
			if (!location.exists())
				throw new FileNotFoundException(
						String.format("The directory %s does not exist.", location.getCanonicalPath()));

			fileLocation = new File(location, this.filename);
			int i = 1;
			while (fileLocation.exists()) {
				fileLocation = new File(location, getIndexedFilename(this.filename, i++));
			}
		}

		// saving the file content to the calculated location.
		try (FileOutputStream fileStream = new FileOutputStream(fileLocation)) {
			fileStream.write(fileContent);
		}
	}

	/*
	 * Borrowed from http://stackoverflow.com/a/4546093 Does not support
	 * files like x.tar.gz (renames them to x.tar (index).gz
	 */
	private String getIndexedFilename(String filename, int index) {
		String[] parts = new String[] { filename };

		if (filename.contains("."))
			parts = filename.split("\\.(?=[^\\.]+$)");

		return String.format("%s_(%d)%s", parts[0], index, (parts.length > 1 ? "." + parts[1] : ""));
	}

	public String toString() {
		return String.format("File \"%s\"(%d kB) from %s to %s", this.filename,
				this.fileContent != null ? this.fileContent.length / 1024 : "null", this.sender, this.receiver);
	}
}
