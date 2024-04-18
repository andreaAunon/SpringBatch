package es.neesis.demospringbatch.writer;

import es.neesis.demospringbatch.model.UserEntity;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

public class UserWriter implements ItemWriter<UserEntity> {

    private final DataSource dataSource;
    private ExecutionContext executionContext;
    private String sqlStatement;

    public UserWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.executionContext = stepExecution.getJobExecution().getExecutionContext();
    }

    @Override
    public void write(List<? extends UserEntity> list) throws Exception {
        for(UserEntity user : list){
            String operation = user.getOperation();
            switch (operation.toUpperCase()){
                case "INSERT":
                    addUser(user);
                    break;
                case "UPDATE":
                    updateUser(user);
                    break;
                case "DELETE":
                    deleteUser(user);
                    break;
            }

            JdbcBatchItemWriter<UserEntity> builder = new JdbcBatchItemWriterBuilder<UserEntity>()
                .beanMapped()
                .sql(this.sqlStatement)
                .dataSource(dataSource)
                .build();
            builder.afterPropertiesSet();

            List<UserEntity> userEntities = list.stream().map(UserEntity.class::cast).collect(Collectors.toList());
            builder.write(userEntities);
            actualizeContext(userEntities);
        }
    }

    private void actualizeContext(List<UserEntity> userEntities){
        List<UserEntity> users = (List<UserEntity>) this.executionContext.get("users");
        if(users == null) {
            users = userEntities;
        } else {
            users.addAll(userEntities);
        }
        this.executionContext.put("users", users);
    }

    private void addUser(UserEntity user){
        this.sqlStatement = "INSERT INTO users (id, username, password, email) VALUES (:id, :username, :password, :email)";
    }

    private void deleteUser(UserEntity user){
        this.sqlStatement = "DELETE FROM users WHERE username=:username";
    }

    private void updateUser(UserEntity user){
        this.sqlStatement = "UPDATE users SET id=:id, username=:username, password=:password,email=:email " +
                "WHERE id=:id and username=:username and password=:password and email=:email";
    }
}
