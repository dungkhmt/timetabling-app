package openerp.openerpresourceserver.wms.entity.sequence;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * StringPrefixSequenceGenerator là một lớp mở rộng của SequenceStyleGenerator nhằm tạo ra khóa chính tự động
 * với tiền tố (prefix) và định dạng số tùy chỉnh.
 */
public class StringPrefixSequenceGenerator extends SequenceStyleGenerator {
    public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;

    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;

    public static final String SEQUENCE_TABLE_PARAMETER = "sequenceTable";
    public static final String SEQUENCE_TABLE_DEFAULT = "";
    private String sequenceTable;

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(type, params, serviceRegistry);
        valuePrefix = ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER, params, VALUE_PREFIX_DEFAULT);
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER, params, NUMBER_FORMAT_DEFAULT);
        sequenceTable = ConfigurationHelper.getString(SEQUENCE_TABLE_PARAMETER, params, SEQUENCE_TABLE_DEFAULT);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String sql = "SELECT nextval('" + sequenceTable + "')"; // Sử dụng nextval()

        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);

            if (rs.next()) {
                long seqValue = rs.getLong(1);
                return valuePrefix + String.format(numberFormat, seqValue);
            } else {
                throw new HibernateException("Không thể lấy giá trị từ sequence: " + sequenceTable);
            }
        } catch (SQLException e) {
            throw new HibernateException("Không thể sinh chuỗi từ sequence", e);
        } finally {
            // Đảm bảo đóng tất cả tài nguyên theo thứ tự ngược lại
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // Log lỗi nếu cần, nhưng không ném lại exception trong finally
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // Log lỗi nếu cần
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Log lỗi nếu cần
                }
            }
        }
    }
}