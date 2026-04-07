export interface NotificationDto {
  id: string;
  type: string;
  title: string;
  message: string;
  isRead: boolean;
  referenceType: string;
  referenceId: string;
  createdAt: string;
}
