INSERT IGNORE INTO courses (id, course_name, start_date, end_date) VALUES
(1, 'Programming Fundamentals - септември 2024 (Practical Exam)', '2024-12-01', '2024-12-01'),
(2, 'Programming Basics - септември 2024', '2024-10-26', '2024-10-27'),
(3, 'Programming Fundamentals - септември 2024 (Mid Exam)', '2024-10-19', '2024-10-19'),
(4, 'Programming Basics - септември 2024', '2024-10-19', '2024-10-20'),
(5, 'Programming Fundamentals - септември 2024 (Retake Mid Exam)', '2024-12-03', '2024-12-03'),
(6, 'Programming Fundamentals - септември 2024 (Retake Practical Exam)', '2024-12-05', '2024-12-05');

INSERT IGNORE INTO manager_status (id, comment_id, current_status, guild_id) VALUES
(1, '1282160192271679600', 'UNLOCKED', '1094748029866758219'),
(2, '1282160213553840219', 'UNLOCKED', '886268434004983808'),
(3, '1282160214061350914', 'UNLOCKED', '954298970799243285');

INSERT IGNORE INTO seminars (id, date, image_url, lecturers, link, time, title) VALUES
(1, '17 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FConfiguration-Management-Database.png', 'Светлан Георгиев', 'https://softuni.bg/trainings/4793/configuration-management-database-cmdb', '19:00', 'Configuration Management Database (CMDB)'),
(2, '18 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FMilitary-developments-with-artificial-intelligence.png', 'д-р Мартин, Калоев', 'https://softuni.bg/trainings/4786/military-developments-with-artificial-intelligence', '19:00', 'Военни разработки с изкуствен интелект'),
(3, '18 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FCreate-a-game-with-Unreal-Engine-in-one-hour.png', 'Антон Десов', 'https://softuni.bg/trainings/4791/create-a-game-with-unreal-engine-in-one-hour', '19:00', 'Създаване на игра за 1 час в Unreal Engine'),
(4, '24 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FMinimal-Web-APIs-in-ASPNET-Core-and-NET-8.png', 'Тонислав Троев', 'https://softuni.bg/trainings/4794/minimal-web-apis-in-asp-dot-net-core-and-dot-net-8', '19:00', 'Minimal Web APIs in ASP.NET Core and .NET 8'),
(5, '26 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FInnovations-in-the-Crypto-Industry.png', 'Илиян Клонов', 'https://softuni.bg/trainings/4792/innovations-in-the-crypto-industry-new-blockchain-networks-defi-protocols-and-current-trends', '19:00', 'Иновации в крипто индустрията - нови блокчейн мрежи, DEFI протоколи и текущи трендове'),
(6, '02 октомври 2024', 'https://softuni.bg/Files%2FCRM-Trainers-Photos%2FSeminar-Template%2FThe-Journey-from-Manual-to-Automation-QA-A-Roadmap.jpg', 'Стилиян Бешев', 'https://softuni.bg/trainings/4800/the-journey-from-manual-to-automation-qa-a-roadmap', '19:00', 'Първи стъпки в приключението наречено автоматизирано тестване'),
(7, '18 септември 2024', 'https://softuni.bg/Files%2FImages%2FSvetlin.Nakov.jpg', 'Светлин Наков', 'https://softuni.bg/trainings/4805/The-future-of-artificial-intelligence', '19:00', 'Бъдещето на изкуствения интелект Live Q&A със Светлин Наков'),
(8, '17 септември 2024', 'https://softuni.bg/files%2Fcrm-trainers-photos%2Fseminar-template%2FConfiguration-Management-Database.png', 'Светлан Георгиев', 'https://softuni.bg/trainings/4793/configuration-management-database-cmdb', '19:00', 'Configuration Management Database (CMDB)');