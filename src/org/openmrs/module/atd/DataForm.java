package org.openmrs.module.atd;

public class DataForm {

	private String defaultLocation = "";
	private String defaultLocale = "";
	private Boolean showRetiredMessage = true;
	private Boolean verbose = false;
	
	private String username = "";
	private String oldPassword = "";
	private String newPassword = "";
	private String confirmPassword = "";
	
	private String secretQuestionPassword = "";
	private String secretQuestionNew = "";
	private String secretAnswerNew = "";
	private String secretAnswerConfirm = "";
	
	private String notification = "";
	
	public DataForm() {}

	/**
	 * @return the defaultLocation
	 */
	public String getDefaultLocation()
	{
		return this.defaultLocation;
	}

	/**
	 * @param defaultLocation the defaultLocation to set
	 */
	public void setDefaultLocation(String defaultLocation)
	{
		this.defaultLocation = defaultLocation;
	}

	/**
	 * @return the defaultLocale
	 */
	public String getDefaultLocale()
	{
		return this.defaultLocale;
	}

	/**
	 * @param defaultLocale the defaultLocale to set
	 */
	public void setDefaultLocale(String defaultLocale)
	{
		this.defaultLocale = defaultLocale;
	}

	/**
	 * @return the showRetiredMessage
	 */
	public Boolean getShowRetiredMessage()
	{
		return this.showRetiredMessage;
	}

	/**
	 * @param showRetiredMessage the showRetiredMessage to set
	 */
	public void setShowRetiredMessage(Boolean showRetiredMessage)
	{
		this.showRetiredMessage = showRetiredMessage;
	}

	/**
	 * @return the verbose
	 */
	public Boolean getVerbose()
	{
		return this.verbose;
	}

	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(Boolean verbose)
	{
		this.verbose = verbose;
	}

	/**
	 * @return the username
	 */
	public String getUsername()
	{
		return this.username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return the oldPassword
	 */
	public String getOldPassword()
	{
		return this.oldPassword;
	}

	/**
	 * @param oldPassword the oldPassword to set
	 */
	public void setOldPassword(String oldPassword)
	{
		this.oldPassword = oldPassword;
	}

	/**
	 * @return the newPassword
	 */
	public String getNewPassword()
	{
		return this.newPassword;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPassword(String newPassword)
	{
		this.newPassword = newPassword;
	}

	/**
	 * @return the confirmPassword
	 */
	public String getConfirmPassword()
	{
		return this.confirmPassword;
	}

	/**
	 * @param confirmPassword the confirmPassword to set
	 */
	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	/**
	 * @return the secretQuestionPassword
	 */
	public String getSecretQuestionPassword()
	{
		return this.secretQuestionPassword;
	}

	/**
	 * @param secretQuestionPassword the secretQuestionPassword to set
	 */
	public void setSecretQuestionPassword(String secretQuestionPassword)
	{
		this.secretQuestionPassword = secretQuestionPassword;
	}

	/**
	 * @return the secretQuestionNew
	 */
	public String getSecretQuestionNew()
	{
		return this.secretQuestionNew;
	}

	/**
	 * @param secretQuestionNew the secretQuestionNew to set
	 */
	public void setSecretQuestionNew(String secretQuestionNew)
	{
		this.secretQuestionNew = secretQuestionNew;
	}

	/**
	 * @return the secretAnswerNew
	 */
	public String getSecretAnswerNew()
	{
		return this.secretAnswerNew;
	}

	/**
	 * @param secretAnswerNew the secretAnswerNew to set
	 */
	public void setSecretAnswerNew(String secretAnswerNew)
	{
		this.secretAnswerNew = secretAnswerNew;
	}

	/**
	 * @return the secretAnswerConfirm
	 */
	public String getSecretAnswerConfirm()
	{
		return this.secretAnswerConfirm;
	}

	/**
	 * @param secretAnswerConfirm the secretAnswerConfirm to set
	 */
	public void setSecretAnswerConfirm(String secretAnswerConfirm)
	{
		this.secretAnswerConfirm = secretAnswerConfirm;
	}

	/**
	 * @return the notification
	 */
	public String getNotification()
	{
		return this.notification;
	}

	/**
	 * @param notification the notification to set
	 */
	public void setNotification(String notification)
	{
		this.notification = notification;
	}
}