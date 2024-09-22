INSERT IGNORE INTO exams (id, course_name, start_date, end_date) VALUES
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
(6, '02 октомври 2024', 'https://softuni.bg/Files%2FCRM-Trainers-Photos%2FSeminar-Template%2FThe-Journey-from-Manual-to-Automation-QA-A-Roadmap.jpg', 'Стилиян Бешев', 'https://softuni.bg/trainings/4800/the-journey-from-manual-to-automation-qa-a-roadmap', '19:00', 'Първи стъпки в приключението наречено автоматизирано тестване'),
