export interface Notification {
  id?: string;
  sender: string;
  recipient: string;
  subject: string;
  body: string;
  createdAt?: string;
}