package de.ts.chat.server.beans;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import de.ts.chat.server.beans.exception.InvalidLoginException;
import de.ts.chat.server.beans.exception.MultipleLoginException;
import de.ts.chat.server.beans.interfaces.UserManagementLocal;
import de.ts.chat.server.beans.interfaces.UserSessionLocal;
import de.ts.chat.server.beans.interfaces.UserSessionRemote;
import de.ts.server.beans.entities.ChatUser;

@Stateful
public class UserSessionBean implements UserSessionRemote, UserSessionLocal,
		Serializable {

	private static final long serialVersionUID = -8902107191952463029L;

	private ChatUser user;

	@EJB
	private UserManagementLocal userManagementLocal;

	public UserSessionBean() {
		// TODO Auto-generated constructor stub
	}

	// @Resource(name = "hashAlgorithm")
	// TODO: Deployment Descriptor fixen
	private static String hashAlgorithm = "SHA-1";

	public static String generateHash(String plaintext) {

		if (hashAlgorithm == null) {
			hashAlgorithm = "SHA-1";
		}

		String hash;
		try {
			MessageDigest encoder = MessageDigest.getInstance(hashAlgorithm);
			hash = String.format("%040x",
					new BigInteger(1, encoder.digest(plaintext.getBytes())));
		} catch (NoSuchAlgorithmException e) {
			hash = null;
		}
		return hash;
	}

	@Override
	public void changePassword(String oldPW, String newPW) throws Exception {

		String passwordHash = user.getPasswordHash();

		String hashOld = generateHash(oldPW);
		if (!hashOld.equals(passwordHash)) {
			throw new InvalidLoginException("Falsches Passwort!");
		}

		user.setPasswordHash(generateHash(newPW));
	}

	@Override
	public void disconnect() throws Exception {
		logout();
	}

	@Remove
	@Override
	public void delete(String password) throws Exception {
		String actualPasswordHash = user.getPasswordHash();
		String givenPasswordHash = generateHash(password);
		if (!actualPasswordHash.equals(givenPasswordHash)) {
			throw new InvalidLoginException("Falsches Passwort!");
		} else {
			userManagementLocal.delete(user);
			disconnect();
		}
	}

	@Override
	public String getUserName() {
		return user.getName();
	}

	@Remove
	@Override
	public void logout() throws Exception {
		userManagementLocal.logout(user);

	}

	@Override
	public void login(String userName, String password)
			throws MultipleLoginException, InvalidLoginException {
		user = userManagementLocal.login(userName, password);
	}

	@Remove
	@Override
	public void remove() {

	}
}
