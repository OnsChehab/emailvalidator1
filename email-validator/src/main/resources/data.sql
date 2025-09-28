-- Données de démonstration
INSERT INTO emails (address, status, is_active) VALUES
                                                    ('admin@universite.edu', 'valid', true),
                                                    ('professeur@univ-ac.fr', 'valid', true),
                                                    ('etudiant@college.edu', 'valid', true),
                                                    ('contact@school.edu', 'valid', true),
                                                    ('ancien@hotmail.com', 'obsolete', false),
                                                    ('test@yahoo.com', 'obsolete', false),
                                                    ('invalide-email', 'invalid', false);

-- Message de confirmation
-- SELECT 'Données de démonstration insérées avec succès' as status;