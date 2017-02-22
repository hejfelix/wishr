package com.lambdaminute.wishr.notification
import org.apache.commons.mail._

case class EmailCredentials(user: String, password: String)

case class Email(sender: String, creds: EmailCredentials, rootPath: String) {

  def sendTo(recipient: String, token: String) = {
    val email = new SimpleEmail();
    email.setHostName("smtp.googlemail.com")
    email.setSmtpPort(465)
    email.setAuthenticator(new DefaultAuthenticator(creds.user, creds.password))
    email.setSSLOnConnect(true)
    email.setFrom(sender)
    email.setSubject("Activation link")
    email.setMsg(s"""Please go to http://$rootPath/finalize/$token to finalize your registration.""")
    email.addTo(recipient)
    email.send()
  }

}
