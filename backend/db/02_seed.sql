-- Sample data. All companies and jobs are made up.

INSERT INTO skills (name) VALUES
  ('Kotlin'), ('Java'), ('Android'), ('Jetpack Compose'), ('Swift'), ('Flutter'),
  ('JavaScript'), ('TypeScript'), ('React'), ('React Native'), ('Next.js'), ('Node.js'),
  ('HTML'), ('CSS'), ('Python'), ('Django'), ('Go'), ('SQL'), ('PostgreSQL'), ('MongoDB'),
  ('REST APIs'), ('GraphQL'), ('Docker'), ('Kubernetes'), ('AWS'), ('Git'), ('CI/CD'),
  ('Unit Testing'), ('Figma'), ('UI Design'), ('Data Analysis'), ('Excel'), ('Machine Learning'),
  ('Digital Marketing'), ('SEO'), ('Content Writing'), ('Communication'), ('Team Leadership'),
  ('Account Management'), ('Customer Service'), ('Sales'), ('Agile');

-- Demo login: shubham@skillmatch.dev / password123
INSERT INTO users (name, email, password_hash, location, experience_level) VALUES
  ('Shubham', 'shubham@skillmatch.dev', crypt('password123', gen_salt('bf')), 'Bengaluru', 'mid');

INSERT INTO user_skills (user_id, skill_id, level)
SELECT 1, s.id, v.level
FROM (VALUES
  ('Kotlin', 4), ('Android', 4), ('Java', 3), ('Jetpack Compose', 3), ('Git', 3),
  ('REST APIs', 3), ('JavaScript', 3), ('TypeScript', 3), ('React', 2), ('Node.js', 2),
  ('SQL', 2), ('PostgreSQL', 2), ('Unit Testing', 2), ('Agile', 2), ('Communication', 3),
  ('Docker', 1), ('Figma', 1), ('HTML', 2), ('CSS', 2)
) AS v(name, level)
JOIN skills s ON s.name = v.name;

INSERT INTO jobs (title, company, location, employment_type, work_mode, experience_min, experience_max, salary_min_lpa, salary_max_lpa, description, posted_at) VALUES
  ('Android Developer', 'Nimbus Labs', 'Bengaluru, Karnataka', 'Full-time', 'Hybrid', 2, 5, 12, 20,
   'Build and ship features for our consumer Android app used by 2M+ people. You will own screens end to end, from Compose UI to API integration, and work closely with design and backend teams.',
   now() - interval '10 hours'),
  ('Senior Android Engineer', 'PayKart', 'Pune, Maharashtra', 'Full-time', 'On-site', 5, 9, 25, 38,
   'Lead the Android guild for a fintech payments app. Drive architecture decisions, mentor engineers, and improve app performance and stability.',
   now() - interval '1 day'),
  ('Mobile Engineer (Kotlin + Swift)', 'Orbit Health', 'Remote', 'Full-time', 'Remote', 3, 6, 18, 28,
   'Work across our native Android and iOS apps that help patients book consultations and track prescriptions.',
   now() - interval '2 days'),
  ('Full Stack Developer', 'Quillstack', 'Hyderabad, Telangana', 'Full-time', 'Hybrid', 2, 5, 10, 18,
   'Build product features across a Next.js frontend and a Node.js API backed by PostgreSQL.',
   now() - interval '5 hours'),
  ('React Native Developer', 'Tiffin Box', 'Mumbai, Maharashtra', 'Full-time', 'On-site', 1, 4, 8, 14,
   'Own the customer app for a fast-growing food delivery startup. Ship weekly, measure everything.',
   now() - interval '3 days'),
  ('Backend Engineer (Node.js)', 'LedgerLoop', 'Bengaluru, Karnataka', 'Full-time', 'Hybrid', 3, 6, 16, 26,
   'Design REST and GraphQL APIs for an accounting platform used by small businesses.',
   now() - interval '20 hours'),
  ('Frontend Engineer', 'Pixelcraft Studio', 'Remote', 'Contract', 'Remote', 1, 3, 6, 12,
   'Turn Figma designs into fast, accessible React interfaces for client projects.',
   now() - interval '4 days'),
  ('Junior Android Developer', 'EduSpark', 'Chennai, Tamil Nadu', 'Full-time', 'On-site', 0, 2, 4, 7,
   'Join a small team building a learning app for school students. Great place to grow your Android skills.',
   now() - interval '6 hours'),
  ('Flutter Developer', 'GreenRoute', 'Pune, Maharashtra', 'Full-time', 'Hybrid', 2, 4, 9, 15,
   'Build cross-platform apps for an EV charging network.',
   now() - interval '6 days'),
  ('DevOps Engineer', 'CloudNest', 'Hyderabad, Telangana', 'Full-time', 'Hybrid', 3, 7, 18, 30,
   'Run our Kubernetes clusters on AWS and improve our CI/CD pipelines.',
   now() - interval '2 days'),
  ('Python Developer', 'DataWeave Analytics', 'Gurugram, Haryana', 'Full-time', 'On-site', 2, 5, 10, 18,
   'Build data pipelines and Django services for analytics dashboards.',
   now() - interval '1 day'),
  ('Machine Learning Engineer', 'Cortex AI', 'Bengaluru, Karnataka', 'Full-time', 'Hybrid', 3, 6, 22, 35,
   'Train, evaluate and deploy ML models for document understanding.',
   now() - interval '8 days'),
  ('Data Analyst', 'RetailIQ', 'Mumbai, Maharashtra', 'Full-time', 'On-site', 1, 3, 6, 10,
   'Analyse sales data and build reports that help store managers make decisions.',
   now() - interval '3 days'),
  ('UI/UX Designer', 'Pixelcraft Studio', 'Remote', 'Full-time', 'Remote', 2, 5, 8, 16,
   'Design mobile and web experiences from research to high-fidelity prototypes.',
   now() - interval '12 hours'),
  ('Team Lead - Operations', 'Brightway Services', 'Bengaluru, Karnataka', 'Full-time', 'On-site', 4, 8, 9, 14,
   'Lead a team of 12 operations specialists and own client account health.',
   now() - interval '2 days'),
  ('Digital Marketing Executive', 'Bloom Media', 'Delhi, NCR', 'Full-time', 'Hybrid', 1, 3, 4, 7,
   'Plan and run SEO and paid campaigns for D2C brands.',
   now() - interval '5 days'),
  ('Content Writer (Tech)', 'Quillstack', 'Remote', 'Part-time', 'Remote', 0, 3, 3, 6,
   'Write tutorials and docs for developers.',
   now() - interval '7 days'),
  ('Customer Success Associate', 'LedgerLoop', 'Chennai, Tamil Nadu', 'Full-time', 'On-site', 0, 2, 3.5, 5,
   'Help small businesses get the most out of our product.',
   now() - interval '1 day'),
  ('Software Engineer - Mobile Platform', 'PayKart', 'Bengaluru, Karnataka', 'Full-time', 'Hybrid', 2, 5, 18, 28,
   'Build shared Kotlin libraries, CI tooling and release automation for 6 mobile teams.',
   now() - interval '9 hours'),
  ('Android Intern', 'Nimbus Labs', 'Bengaluru, Karnataka', 'Internship', 'On-site', 0, 1, 2.4, 3.6,
   'A 6-month internship working on real features in our Android app.',
   now() - interval '4 days'),
  ('Go Backend Engineer', 'Streamline', 'Remote', 'Full-time', 'Remote', 3, 6, 20, 32,
   'Build high-throughput services in Go on top of PostgreSQL and Kubernetes.',
   now() - interval '10 days'),
  ('QA Automation Engineer', 'Orbit Health', 'Pune, Maharashtra', 'Full-time', 'Hybrid', 2, 5, 8, 14,
   'Automate mobile and API tests and own release quality.',
   now() - interval '3 days'),
  ('Sales Associate', 'UrbanKicks', 'Mumbai, Maharashtra', 'Full-time', 'On-site', 0, 2, 2.5, 4,
   'Help customers find the right products in our flagship store.',
   now() - interval '22 hours'),
  ('iOS Developer', 'Tiffin Box', 'Mumbai, Maharashtra', 'Full-time', 'On-site', 2, 5, 12, 20,
   'Build the iOS version of our customer app in Swift.',
   now() - interval '2 days');

INSERT INTO job_skills (job_id, skill_id)
SELECT v.job_id, s.id
FROM (VALUES
  (1, 'Kotlin'), (1, 'Android'), (1, 'Jetpack Compose'), (1, 'REST APIs'), (1, 'Git'),
  (2, 'Kotlin'), (2, 'Android'), (2, 'Jetpack Compose'), (2, 'Team Leadership'), (2, 'CI/CD'), (2, 'Unit Testing'),
  (3, 'Kotlin'), (3, 'Swift'), (3, 'Android'), (3, 'REST APIs'),
  (4, 'TypeScript'), (4, 'React'), (4, 'Next.js'), (4, 'Node.js'), (4, 'PostgreSQL'),
  (5, 'React Native'), (5, 'JavaScript'), (5, 'TypeScript'), (5, 'REST APIs'),
  (6, 'Node.js'), (6, 'TypeScript'), (6, 'REST APIs'), (6, 'GraphQL'), (6, 'PostgreSQL'), (6, 'Docker'),
  (7, 'React'), (7, 'HTML'), (7, 'CSS'), (7, 'JavaScript'), (7, 'Figma'),
  (8, 'Java'), (8, 'Kotlin'), (8, 'Android'), (8, 'Git'),
  (9, 'Flutter'), (9, 'REST APIs'), (9, 'Git'),
  (10, 'Docker'), (10, 'Kubernetes'), (10, 'AWS'), (10, 'CI/CD'),
  (11, 'Python'), (11, 'Django'), (11, 'SQL'), (11, 'PostgreSQL'),
  (12, 'Python'), (12, 'Machine Learning'), (12, 'Data Analysis'), (12, 'Docker'),
  (13, 'SQL'), (13, 'Excel'), (13, 'Data Analysis'), (13, 'Communication'),
  (14, 'Figma'), (14, 'UI Design'), (14, 'Communication'),
  (15, 'Team Leadership'), (15, 'Account Management'), (15, 'Communication'), (15, 'Customer Service'),
  (16, 'Digital Marketing'), (16, 'SEO'), (16, 'Content Writing'),
  (17, 'Content Writing'), (17, 'SEO'), (17, 'JavaScript'),
  (18, 'Customer Service'), (18, 'Communication'), (18, 'Account Management'),
  (19, 'Kotlin'), (19, 'Android'), (19, 'CI/CD'), (19, 'Git'), (19, 'Unit Testing'),
  (20, 'Kotlin'), (20, 'Android'), (20, 'Git'),
  (21, 'Go'), (21, 'PostgreSQL'), (21, 'Kubernetes'), (21, 'Docker'),
  (22, 'Unit Testing'), (22, 'Java'), (22, 'Android'), (22, 'CI/CD'), (22, 'Agile'),
  (23, 'Sales'), (23, 'Customer Service'), (23, 'Communication'),
  (24, 'Swift'), (24, 'REST APIs'), (24, 'Git')
) AS v(job_id, name)
JOIN skills s ON s.name = v.name;
