package email

import (
	"bytes"
	"context"
	"encoding/base64"
	"fmt"
	"mime/quotedprintable"
	"net/smtp"
	"strings"
)

type SMTPSender struct {
	address     string
	username    string
	password    string
	fromAddress string
	fromName    string
}

func NewSMTPSender(address string, username string, password string, fromAddress string, fromName string) *SMTPSender {
	return &SMTPSender{
		address:     address,
		username:    username,
		password:    password,
		fromAddress: fromAddress,
		fromName:    fromName,
	}
}

func (s *SMTPSender) Send(_ context.Context, message Message) error {
	if len(message.To) == 0 {
		return fmt.Errorf("at least one recipient is required")
	}

	host := s.address
	if colon := strings.Index(host, ":"); colon >= 0 {
		host = host[:colon]
	}

	var auth smtp.Auth
	if s.username != "" {
		auth = smtp.PlainAuth("", s.username, s.password, host)
	}

	rawMessage, err := buildMIMEMessage(s.fromName, s.fromAddress, message)
	if err != nil {
		return err
	}

	recipients := append([]string{}, message.To...)
	return smtp.SendMail(s.address, auth, s.fromAddress, recipients, rawMessage)
}

func buildMIMEMessage(fromName string, fromAddress string, message Message) ([]byte, error) {
	var body bytes.Buffer
	boundary := "lombardio-mixed-boundary"

	writeHeader(&body, "From", formatAddress(fromName, fromAddress))
	writeHeader(&body, "To", strings.Join(message.To, ", "))
	if len(message.ReplyTo) > 0 {
		writeHeader(&body, "Reply-To", strings.Join(message.ReplyTo, ", "))
	}
	writeHeader(&body, "Subject", encodeHeader(message.Subject))
	writeHeader(&body, "MIME-Version", "1.0")

	if strings.TrimSpace(message.HTMLBody) == "" {
		writeHeader(&body, "Content-Type", `text/plain; charset="UTF-8"`)
		writeHeader(&body, "Content-Transfer-Encoding", "quoted-printable")
		body.WriteString("\r\n")
		writer := quotedprintable.NewWriter(&body)
		if _, err := writer.Write([]byte(message.TextBody)); err != nil {
			return nil, fmt.Errorf("write text body: %w", err)
		}
		_ = writer.Close()
		return body.Bytes(), nil
	}

	writeHeader(&body, "Content-Type", fmt.Sprintf(`multipart/alternative; boundary="%s"`, boundary))
	body.WriteString("\r\n")

	body.WriteString("--" + boundary + "\r\n")
	body.WriteString("Content-Type: text/plain; charset=\"UTF-8\"\r\n")
	body.WriteString("Content-Transfer-Encoding: quoted-printable\r\n\r\n")
	textWriter := quotedprintable.NewWriter(&body)
	if _, err := textWriter.Write([]byte(message.TextBody)); err != nil {
		return nil, fmt.Errorf("write text body: %w", err)
	}
	_ = textWriter.Close()
	body.WriteString("\r\n")

	body.WriteString("--" + boundary + "\r\n")
	body.WriteString("Content-Type: text/html; charset=\"UTF-8\"\r\n")
	body.WriteString("Content-Transfer-Encoding: quoted-printable\r\n\r\n")
	htmlWriter := quotedprintable.NewWriter(&body)
	if _, err := htmlWriter.Write([]byte(message.HTMLBody)); err != nil {
		return nil, fmt.Errorf("write html body: %w", err)
	}
	_ = htmlWriter.Close()
	body.WriteString("\r\n--" + boundary + "--\r\n")

	return body.Bytes(), nil
}

func writeHeader(buffer *bytes.Buffer, key string, value string) {
	buffer.WriteString(key)
	buffer.WriteString(": ")
	buffer.WriteString(value)
	buffer.WriteString("\r\n")
}

func formatAddress(name string, address string) string {
	if strings.TrimSpace(name) == "" {
		return address
	}
	return fmt.Sprintf("%s <%s>", encodeHeader(name), address)
}

func encodeHeader(value string) string {
	if value == "" {
		return ""
	}
	return "=?UTF-8?B?" + base64.StdEncoding.EncodeToString([]byte(value)) + "?="
}
