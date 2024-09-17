# 🎉 Notify Discord Bot

**Notify** is a custom-built Discord bot created exclusively for the **SoftUni Discord Community**. The bot offers specialized notification services and tailored features designed to improve the overall community experience. It is maintained by SoftUni community members and will not function properly in other Discord servers without modifications.

## 🚀 Features

- 🔔 **Custom Notifications**: Delivers tailored notifications that align with the events, updates, and needs of the SoftUni community.
- 🧭 **Role-based Alerts**: Allows users to receive notifications based on their roles within the community, ensuring they stay informed about what matters to them.
- 📅 **Event Reminders**: Notifies users about upcoming events, deadlines, and other important happenings within the SoftUni Discord.
- 🔗 **Channel-Specific Announcements**: Sends announcements to predefined channels, ensuring important messages reach the right audiences.

## ⚙️ Getting Started

### 📋 Prerequisites

Before you install and run Notify, ensure you have the following set up:

- [Java 11+](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Maven](https://maven.apache.org/install.html)
- A **Discord Bot Token** from the [Discord Developer Portal](https://discord.com/developers/applications)

### 🛠️ Installation

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/Borovaneca/Notify.git
   cd Notify
   ```

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

### 🔧 Configuration

1. Create application-env.properties in root dir.
2. Update the var with your Discord bot token:
   ```preorities
   BOT_TOKEN=your-discord-bot-token
   ```

### ▶️ Running the Bot

To run the bot locally, use the following command:
   ```bash
   mvn spring-boot:run
   ```

Alternatively, after packaging the project:
   ```bash
   java -jar target/notify-1.0.jar
   ```

### 🛑 Important Note:

This bot is **exclusively designed** for the **SoftUni Discord Community**. While you can run the bot on other servers, some features may not work as expected without customization.

### 💼 Maintainers

This bot is maintained by members of the SoftUni Discord Community. If you encounter any issues or have suggestions, feel free to reach out to the community maintainers.

### 📜 License
This project is licensed under the MIT License.
