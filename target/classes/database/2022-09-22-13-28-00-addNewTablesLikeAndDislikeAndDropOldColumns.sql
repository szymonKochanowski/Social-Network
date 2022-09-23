
-- Create new tables.

use `portal-spolecznosciowy`;

create table likes (
    id int AUTO_INCREMENT,
    user_like_id int,
    post_like_id int,
    comment_like_id int,
    PRIMARY KEY (`id`)
);

create table dislikes (
    id int AUTO_INCREMENT,
    user_dislike_id int,
    post_dislike_id int,
    comment_dislike_id int,
    PRIMARY KEY (`id`)
);

-- Drop old columns
alter table posts drop column number_of_likes;
alter table comments drop column number_of_likes;
alter table posts drop column number_of_dislikes;
alter table comments drop column number_of_dislikes;
