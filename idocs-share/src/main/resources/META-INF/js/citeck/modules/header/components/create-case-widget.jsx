import React from 'react';
import { connect } from 'react-redux';
import { Dropdown } from 'react-bootstrap';
import CustomToggle from './dropdown-menu-custom-toggle';
import DropDownMenuGroup from './dropdown-menu-group';

const CreateCaseWidget = ({ items }) => {
    const menuListItems = items && items.length > 0 ? items.map((item, key) => {
        return (
            <DropDownMenuGroup
                key={key}
                label={item.label}
                items={item.items}
                id={item.id}
            />
        );
    }) : null;

    return (
        <div id='HEADER_CREATE_CASE'>
            <Dropdown id="HEADER_CREATE_CASE__DROPDOWN" className="custom-dropdown-menu" pullLeft>
                <CustomToggle bsRole="toggle" className="create-case-dropdown-menu__toggle custom-dropdown-menu__toggle">
                    <i className={"fa fa-plus"} />
                </CustomToggle>
                <Dropdown.Menu bsRole="menu" className="custom-dropdown-menu__body">
                    {menuListItems}
                </Dropdown.Menu>
            </Dropdown>
        </div>
    )
};

const mapStateToProps = (state) => ({
    items: state.caseMenu.items
});

export default connect(mapStateToProps)(CreateCaseWidget);
