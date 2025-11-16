import { NotificationReader } from 'capacitor-notification-reader';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    NotificationReader.echo({ value: inputValue })
}
