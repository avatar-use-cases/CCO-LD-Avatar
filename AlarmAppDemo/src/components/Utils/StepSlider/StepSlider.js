import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Slider from '@material-ui/lab/Slider';

const styles = {
  root: {
    width: 300,
  },
  slider: {
    padding: '22px 0px',
  },
};

class StepSlider extends React.Component {
  state = {
    value: 5,
  };

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { classes } = this.props;
    const { value } = this.state;

    return (
      <div id = "slider" className={classes.root} style={{textAlign:'center',width:'60%',marginLeft:'auto',marginRight:'auto'}}>
        <Slider
          classes={{ container: classes.slider }}
          value={value}
          min={0}
          max={120}
          step={5}
          onChange={this.handleChange}
        />

        {this.state.value} minutes
      </div>
    );
  }
}

StepSlider.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(StepSlider);