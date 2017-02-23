package com.lambdaminute.wishr.notification
import com.lambdaminute.wishr.config.EmailSettings
import org.apache.commons.mail._

case class Email(emailSettings: EmailSettings, rootPath: String) {

  def sendTo(recipient: String, token: String) = {
    val email = new SimpleEmail();
    email.setHostName(emailSettings.smtp)
    email.setSmtpPort(emailSettings.port)
    email.setAuthenticator(new DefaultAuthenticator(emailSettings.user, emailSettings.password))
    email.setSSLOnConnect(true)
    email.setFrom(emailSettings.sender)
    email.setSubject("Activation link")
    email.setMsg(s"""Please go to http://$rootPath/finalize/$token to finalize your registration.""")
    email.addTo(recipient)
    email.send()
  }

}
